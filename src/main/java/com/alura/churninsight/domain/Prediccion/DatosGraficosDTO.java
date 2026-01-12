package com.alura.churninsight.domain.Prediccion;

public record DatosGraficosDTO(
        long total,
        long churn,
        long retencion,
        long planBasico,
        long planEstandar,
        long planPremium,
        long riesgoBajo,
        long riesgoMedio,
        long riesgoAlto) {
}
