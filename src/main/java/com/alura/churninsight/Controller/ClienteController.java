package com.alura.churninsight.Controller;

import com.alura.churninsight.Services.ClienteService;
import com.alura.churninsight.domian.Cliente.Cliente;
import com.alura.churninsight.domian.Cliente.ClienteDatos;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> guardarCliente(
            @RequestBody @Valid ClienteDatos datos) {

        Cliente cliente = service.guardar(datos);
        return ResponseEntity.ok(cliente);
    }
}
