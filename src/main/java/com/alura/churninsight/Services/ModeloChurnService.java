package com.alura.churninsight.Services;

import java.util.List;

@org.springframework.stereotype.Service
public class ModeloChurnService {

    private final org.springframework.web.client.RestTemplate restTemplate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModeloChurnService.class);
    @org.springframework.beans.factory.annotation.Value("${api.ds.url}")
    private String PYTHON_API_URL;

    public ModeloChurnService(org.springframework.web.client.RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResultadoModelo calcularProbabilidadCancelacion(com.alura.churninsight.domain.Cliente.Cliente cliente) {
        double probabilidad;
        java.util.List<String> factores = new java.util.ArrayList<>();

        // 1. Intentar obtener predicción del modelo de Data Science (Python)
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("tiempo_meses", cliente.getTiempoMeses());
            payload.put("retrasos_pago", cliente.getRetrasosPago());
            payload.put("uso_mensual_horas", cliente.getUsoMensualHrs());
            payload.put("plan", cliente.getPlan().name());
            payload.put("soporte_tickets", cliente.getSoporteTickets());
            payload.put("cambio_plan", cliente.getCambioPlan() ? 1 : 0);
            payload.put("pago_automatico", cliente.getPagoAutomatico() ? 1 : 0);
            payload.put("Genero", cliente.getGenero().name());

            log.info("Llamando a la API de Data Science en: {}", PYTHON_API_URL);

            // Configurar headers para saltar el aviso de localtunnel (Bypass)
            // automáticamente
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("bypass-tunnel-reminder", "true");
            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(
                    payload, headers);

            @SuppressWarnings("rawtypes")
            org.springframework.http.ResponseEntity<java.util.Map> responseEntity = restTemplate.postForEntity(
                    PYTHON_API_URL, entity, java.util.Map.class);

            java.util.Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("probabilidad")) {
                probabilidad = ((Number) response.get("probabilidad")).doubleValue();
                log.info("Predicción recibida de Python: {}", probabilidad);
            } else {
                throw new RuntimeException("Respuesta inválida de Python");
            }
        } catch (Exception e) {
            log.warn("No se pudo conectar con el modelo de Python ({}). Usando lógica de respaldo local.",
                    e.getMessage());
            probabilidad = calcularProbabilidadLocal(cliente, factores);
        }

        // 2. Generar explicabilidad (Factores de Riesgo) en Java
        // (Se mantiene siempre para enriquecer el Dashboard)
        if (factores.isEmpty()) {
            generarFactoresExplicativos(cliente, factores);
        }

        java.util.List<String> mejoresFactores = factores.stream().limit(3).toList();
        return new ResultadoModelo(Math.min(probabilidad, 1.0), mejoresFactores);
    }

    private double calcularProbabilidadLocal(com.alura.churninsight.domain.Cliente.Cliente cliente,
            java.util.List<String> factores) {
        double prob = 0.0;
        if (cliente.getRetrasosPago() > 1)
            prob += 0.30;
        if (cliente.getSoporteTickets() > 4)
            prob += 0.25;
        if (cliente.getUsoMensualHrs() < 5)
            prob += 0.20;
        if (cliente.getTiempoMeses() != null && cliente.getTiempoMeses() < 12)
            prob += 0.15;
        if ("BASICO".equals(cliente.getPlan().name()))
            prob += 0.10;
        if (cliente.getPagoAutomatico() == null || !cliente.getPagoAutomatico())
            prob += 0.10;
        if (Boolean.TRUE.equals(cliente.getCambioPlan()))
            prob += 0.15;
        return prob;
    }

    private void generarFactoresExplicativos(com.alura.churninsight.domain.Cliente.Cliente cliente,
            java.util.List<String> factores) {
        if (cliente.getRetrasosPago() > 1)
            factores.add("Múltiples retrasos en pagos registrados");
        if (cliente.getSoporteTickets() > 4)
            factores.add("Excesiva interacción con soporte técnico");
        if (cliente.getUsoMensualHrs() < 5)
            factores.add("Abandono de uso del servicio (Inactividad)");
        if (cliente.getTiempoMeses() != null && cliente.getTiempoMeses() < 12)
            factores.add("Cliente en periodo crítico de retención (<1 año)");
        if ("BASICO".equals(cliente.getPlan().name()))
            factores.add("Plan con baja fidelización (Básico)");
        if (cliente.getPagoAutomatico() == null || !cliente.getPagoAutomatico())
            factores.add("Método de pago manual (Riesgo de olvido)");
        if (Boolean.TRUE.equals(cliente.getCambioPlan()))
            factores.add("Inactividad tras cambio de plan reciente");
    }

    public record ResultadoModelo(double probabilidad, List<String> factores) {
    }
}
