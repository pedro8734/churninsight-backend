package com.alura.churninsight.Services;

import com.alura.churninsight.Repository.ClienteRepository;
import com.alura.churninsight.domian.Cliente.Cliente;
import com.alura.churninsight.domian.Cliente.ClienteDatos;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente guardar(ClienteDatos datos) {

        if (repository.existsByClienteId(datos.clienteId())) {
            throw new IllegalArgumentException("Cliente ya existe");
        }

        Cliente cliente = new Cliente();
        cliente.setClienteId(datos.clienteId());
        cliente.setGenero(datos.genero());
        cliente.setTiempoMeses(datos.tiempoMeses());
        cliente.setRetrasosPago(datos.retrasosPago());
        cliente.setUsoMensualHrs(datos.usoMensualHrs());
        cliente.setPlan(datos.plan());
        cliente.setSoporteTickets(datos.soporteTickets());
        cliente.setCambioPlan(datos.cambioPlan());
        cliente.setPagoAutomatico(datos.pagoAutomatico());
        cliente.setEdad(datos.edad());

        return repository.save(cliente);
    }
}
