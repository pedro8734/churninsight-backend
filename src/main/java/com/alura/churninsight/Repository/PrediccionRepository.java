package com.alura.churninsight.Repository;

import com.alura.churninsight.domain.Prediccion.Prediccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {

    @Query("SELECT COUNT(DISTINCT p.cliente) FROM Prediccion p")
    long countTotalEvaluados();

    @Query("SELECT COUNT(DISTINCT p.cliente) FROM Prediccion p WHERE p.probabilidad >= 0.6")
    long countChurnProbable();

    @Query("SELECT p FROM Prediccion p LEFT JOIN FETCH p.cliente LEFT JOIN FETCH p.factores WHERE p.probabilidad >= 0.6 AND p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) ORDER BY p.fecha DESC")
    java.util.List<Prediccion> buscarPrediccionesAltoRiesgo();

    @Query("SELECT p FROM Prediccion p LEFT JOIN FETCH p.cliente LEFT JOIN FETCH p.factores WHERE p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) ORDER BY p.fecha DESC")
    java.util.List<Prediccion> buscarTodasLasUltimasPredicciones();

    // Optimizaciones para Dashboard (Cálculos en DB)
    @Query("SELECT COUNT(p) FROM Prediccion p WHERE p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) AND p.cliente.plan = :plan")
    long countByUltimaPrediccionYPlan(com.alura.churninsight.domain.Cliente.PlanStatus plan);

    @Query("SELECT COUNT(p) FROM Prediccion p WHERE p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) AND p.probabilidad < :max")
    long countRiesgoBajo(double max);

    @Query("SELECT COUNT(p) FROM Prediccion p WHERE p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) AND p.probabilidad >= :min AND p.probabilidad < :max")
    long countRiesgoMedio(double min, double max);

    @Query("SELECT COUNT(p) FROM Prediccion p WHERE p.id IN (SELECT MAX(p2.id) FROM Prediccion p2 GROUP BY p2.cliente.id) AND p.probabilidad >= :min")
    long countRiesgoAlto(double min);
}
