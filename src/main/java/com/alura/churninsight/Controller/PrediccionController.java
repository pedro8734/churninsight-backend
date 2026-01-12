package com.alura.churninsight.Controller;

import com.alura.churninsight.Services.PrediccionService;
import com.alura.churninsight.domain.Prediccion.DatosSolicitudPrediccion;
import com.alura.churninsight.domain.Prediccion.DatosEstadisticas;
import com.alura.churninsight.domain.Prediccion.HistorialDTO;
import com.alura.churninsight.domain.Prediccion.ResultadoPrediccion;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/predict")
public class PrediccionController {
    private final PrediccionService prediccionService;

    public PrediccionController(PrediccionService prediccionService) {
        this.prediccionService = prediccionService;
    }

    @PostMapping
    public ResponseEntity<ResultadoPrediccion> realizarPrediccionIndividual(
            @RequestBody @Valid DatosSolicitudPrediccion datos) {
        ResultadoPrediccion resultado = prediccionService.predecirIndividual(datos);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ResultadoPrediccion> consultarPorClienteId(@PathVariable Integer clienteId) {
        ResultadoPrediccion resultado = prediccionService.predecirPorClienteId(clienteId);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/stats")
    public ResponseEntity<DatosEstadisticas> obtenerEstadisticas() {
        return ResponseEntity.ok(prediccionService.obtenerEstadisticas());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ResultadoPrediccion>> predecirEnLote(
            @RequestParam("file") MultipartFile archivo) {
        return ResponseEntity.ok(prediccionService.predecirEnLote(archivo));
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<HistorialDTO>> obtenerClientesAltoRiesgo() {
        return ResponseEntity.ok(prediccionService.obtenerClientesAltoRiesgo());
    }

    @GetMapping("/stats-charts")
    public ResponseEntity<com.alura.churninsight.domain.Prediccion.DatosGraficosDTO> obtenerDatosGraficos() {
        return ResponseEntity.ok(prediccionService.obtenerDatosGraficos());
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> eliminarTodo() {
        prediccionService.eliminarTodo();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clients/ids")
    public ResponseEntity<java.util.List<Integer>> obtenerIDsDisponibles() {
        return ResponseEntity.ok(prediccionService.obtenerTodosLosClienteIds());
    }
}
