package com.alura.churninsight.Services;

import com.alura.churninsight.Repository.ClienteRepository;
import com.alura.churninsight.domian.Cliente.Cliente;
import com.alura.churninsight.domian.Prediccion.ResultadoPrediccion;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PrediccionService {
    private final ClienteRepository clienteRepository;
    private final ModeloChurnService modeloChurnService;

    public PrediccionService(
            ClienteRepository clienteRepository,
            ModeloChurnService modeloChurnService) {
        this.clienteRepository = clienteRepository;
        this.modeloChurnService = modeloChurnService;
    }

    public ResultadoPrediccion predecirPorId(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Cliente no encontrado con id: " + id));

        double probabilidad =
                modeloChurnService.calcularProbabilidadCancelacion(cliente);

        String prevision = probabilidad >= 0.6
                ? "Va a cancelar"
                : "No va a cancelar";

        return new ResultadoPrediccion(prevision, probabilidad);
    }
}
