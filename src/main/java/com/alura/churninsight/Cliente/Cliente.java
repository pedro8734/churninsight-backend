package com.alura.churninsight.Cliente;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data

public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeneroStatus genero;

    @Column(nullable = false)
    private Integer tiempoMeses;

    @Column(nullable = false)
    private Integer retrasosPago;

    @Column(nullable = false)
    private Double usoMensualHrs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanStatus plan;

    @Column(nullable = false)
    private Integer soporteTickets;

    @Column(nullable = false)
    private String cambioPlan;

    @Column(nullable = false)
    private String pagoAutomatico;

    @Column(nullable = false)
    private Integer edad;

    public Cliente() {}
}
