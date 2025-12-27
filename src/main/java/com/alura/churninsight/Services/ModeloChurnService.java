package com.alura.churninsight.Services;


import com.alura.churninsight.domian.Cliente.Cliente;
import org.springframework.stereotype.Service;

@Service
public class ModeloChurnService {
    public double calcularProbabilidadCancelacion(Cliente cliente) {

        double probabilidad = 0.0;

        if (cliente.getRetrasosPago() > 2) probabilidad += 0.25;
        if (cliente.getSoporteTickets() > 3) probabilidad += 0.20;
        if (cliente.getUsoMensualHrs() < 10) probabilidad += 0.15;
        if (cliente.getTiempoMeses() < 6) probabilidad += 0.10;
        if (cliente.getPlan().name().equals("BASICO")) probabilidad += 0.10;

        return Math.min(probabilidad, 1.0);
    }
}
