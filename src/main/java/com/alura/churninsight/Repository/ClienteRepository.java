package com.alura.churninsight.Repository;

import com.alura.churninsight.domian.Cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByClienteId(Integer clienteId);
}
