package com.alura.churninsight.Services;

import com.alura.churninsight.Repository.ClienteRepository;
import com.alura.churninsight.Repository.PrediccionRepository;
import com.alura.churninsight.domain.Cliente.Cliente;
import com.alura.churninsight.domain.Prediccion.Prediccion;
import com.alura.churninsight.domain.Prediccion.DatosSolicitudPrediccion;
import com.alura.churninsight.domain.Prediccion.ResultadoPrediccion;
import com.alura.churninsight.domain.Prediccion.DatosEstadisticas;
import com.alura.churninsight.domain.Prediccion.HistorialDTO;
import com.alura.churninsight.domain.Prediccion.DatosGraficosDTO;
import com.alura.churninsight.Infra.ValidacionDeNegocioException;
import com.alura.churninsight.domain.Cliente.PlanStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class PrediccionService {
        private final ClienteRepository clienteRepository;
        private final ModeloChurnService modeloChurnService;
        private final PrediccionRepository prediccionRepository;
        private final ClienteService clienteService;

        public PrediccionService(ClienteRepository clienteRepository,
                        ModeloChurnService modeloChurnService,
                        PrediccionRepository prediccionRepository,
                        ClienteService clienteService) {
                this.clienteRepository = clienteRepository;
                this.modeloChurnService = modeloChurnService;
                this.prediccionRepository = prediccionRepository;
                this.clienteService = clienteService;
        }

        public ResultadoPrediccion predecirIndividual(DatosSolicitudPrediccion datos) {
                if (clienteRepository.existsByClienteId(datos.idCliente())) {
                        throw new ValidacionDeNegocioException("Ya existe un cliente con el ID " + datos.idCliente()
                                        + ". Use la sección de consulta o asigne un nuevo ID.");
                }

                Cliente cliente = clienteService.registrarDesdeDTO(datos);
                return procesarPrediccion(cliente);
        }

        @Transactional
        public ResultadoPrediccion predecirPorClienteId(Integer id) {
                Cliente cliente = clienteRepository.findByClienteId(id)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
                return procesarPrediccion(cliente);
        }

        @Transactional
        public ResultadoPrediccion predecirPorId(Long id) {
                if (id == null)
                        throw new IllegalArgumentException("ID no puede ser nulo");
                Cliente cliente = clienteRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));
                return procesarPrediccion(cliente);
        }

        private ResultadoPrediccion procesarPrediccion(Cliente cliente) {
                ModeloChurnService.ResultadoModelo resultadoModelo = modeloChurnService
                                .calcularProbabilidadCancelacion(cliente);

                double probabilidad = resultadoModelo.probabilidad();
                boolean isChurn = probabilidad >= 0.6; // Valor solicitado por el equipo (churn)
                String prevision = isChurn ? "Va a cancelar" : "No va a cancelar";

                // Guardar estado en el cliente (como pidió el equipo)
                cliente.setChurn(isChurn);
                clienteRepository.save(cliente);

                Prediccion prediccion = new Prediccion();
                prediccion.setCliente(cliente);
                prediccion.setProbabilidad(probabilidad);
                prediccion.setResultado(prevision);
                prediccion.setFecha(LocalDateTime.now());
                prediccion.setFactores(resultadoModelo.factores());
                prediccionRepository.save(prediccion);

                return new ResultadoPrediccion(
                                isChurn, // boolean churn
                                prevision,
                                probabilidad,
                                resultadoModelo.factores(),
                                cliente.getClienteId(),
                                cliente.getGenero(),
                                cliente.getPlan(),
                                cliente.getTiempoMeses(),
                                cliente.getUsoMensualHrs(),
                                cliente.getSoporteTickets(),
                                cliente.getRetrasosPago(),
                                cliente.getPagoAutomatico(),
                                cliente.getCambioPlan());
        }

        public DatosEstadisticas obtenerEstadisticas() {
                long total = prediccionRepository.countTotalEvaluados();
                long cancelaciones = prediccionRepository.countChurnProbable();
                double tasaCancelacion = total == 0 ? 0 : (double) cancelaciones / total;

                return new DatosEstadisticas(total, tasaCancelacion);
        }

        // Removido @Transactional para evitar bloqueos de DB durante llamadas lentas a
        // IA
        public List<ResultadoPrediccion> predecirEnLote(MultipartFile archivo) {
                try {
                        String contenido = new String(archivo.getBytes());
                        String[] lineas = contenido.split("\n");
                        java.util.List<ResultadoPrediccion> resultados = new java.util.ArrayList<>();

                        for (int i = 1; i < lineas.length; i++) {
                                String linea = lineas[i].trim();
                                if (linea.isEmpty())
                                        continue;
                                int numeroLinea = i + 1;
                                try {
                                        String[] celdas = linea.split(",");
                                        Integer idCliente = Integer.parseInt(celdas[0].trim());

                                        Cliente cliente = clienteService.registrarOActualizar(
                                                        idCliente,
                                                        Integer.parseInt(celdas[1].trim()),
                                                        Integer.parseInt(celdas[2].trim()),
                                                        Double.parseDouble(celdas[3].trim()),
                                                        PlanStatus.valueOf(celdas[4].trim().toUpperCase()),
                                                        Integer.parseInt(celdas[5].trim()),
                                                        null, null, null, null // Defaults
                                        );

                                        resultados.add(procesarPrediccion(cliente));
                                } catch (Exception e) {
                                        log.warn("Error procesando línea {} del CSV: {}", numeroLinea, e.getMessage());
                                }
                        }
                        return resultados;
                } catch (Exception e) {
                        throw new RuntimeException("Error al leer el archivo CSV", e);
                }
        }

        public List<HistorialDTO> obtenerClientesAltoRiesgo() {
                return prediccionRepository.buscarPrediccionesAltoRiesgo().stream()
                                .map(p -> new HistorialDTO(
                                                p.getId(),
                                                p.getCliente().getClienteId(),
                                                p.getProbabilidad(),
                                                p.getResultado(),
                                                p.getCliente().getChurn() != null ? p.getCliente().getChurn() : false,
                                                p.getFecha(),
                                                p.getFactores(),
                                                p.getCliente().getPlan() != null ? p.getCliente().getPlan().toString()
                                                                : "N/A"))
                                .toList();
        }

        public DatosGraficosDTO obtenerDatosGraficos() {
                List<Prediccion> todas = prediccionRepository.buscarTodasLasUltimasPredicciones();

                long churn = todas.stream().filter(p -> p.getCliente().getChurn() != null && p.getCliente().getChurn())
                                .count();
                long ret = todas.size() - churn;
                long b = todas.stream().filter(p -> p.getCliente().getPlan() != null
                                && "BASICO".equals(p.getCliente().getPlan().name())).count();
                long e = todas.stream().filter(p -> p.getCliente().getPlan() != null
                                && "ESTANDAR".equals(p.getCliente().getPlan().name())).count();
                long premiumCount = todas.stream().filter(p -> p.getCliente().getPlan() != null
                                && "PREMIUM".equals(p.getCliente().getPlan().name())).count();
                long rB = todas.stream().filter(p -> p.getProbabilidad() < 0.4).count();
                long rM = todas.stream().filter(p -> p.getProbabilidad() >= 0.4 && p.getProbabilidad() < 0.7).count();
                long rA = todas.stream().filter(p -> p.getProbabilidad() >= 0.7).count();

                return new DatosGraficosDTO(todas.size(), churn, ret, b, e, premiumCount, rB, rM, rA);
        }

        @Transactional
        public void eliminarTodo() {
                prediccionRepository.deleteAll();
                clienteRepository.deleteAll();
        }

        public java.util.List<Integer> obtenerTodosLosClienteIds() {
                return clienteRepository.findAllClienteIds();
        }
}
