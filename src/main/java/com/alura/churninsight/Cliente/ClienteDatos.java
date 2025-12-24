package com.alura.churninsight.Cliente;
import jakarta.validation.constraints.*;

public record ClienteDatos(

        @NotNull Integer clienteId,

        @NotNull GeneroStatus genero,

        @NotNull @Min(1) @Max(120)
        Integer tiempoMeses,

        @NotNull @Min(0)
        Integer retrasosPago,

        @NotNull @PositiveOrZero
        Double usoMensualHrs,

        @NotNull PlanStatus plan,

        @NotNull @Min(0)
        Integer soporteTickets,

        @NotBlank
        String cambioPlan,

        @NotBlank
        String pagoAutomatico,

        @NotNull @Min(18) @Max(100)
        Integer edad

        ) {
}
