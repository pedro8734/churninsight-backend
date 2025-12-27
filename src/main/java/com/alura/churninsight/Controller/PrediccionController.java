package com.alura.churninsight.Controller;


import com.alura.churninsight.Services.PrediccionService;
import com.alura.churninsight.domian.Prediccion.ResultadoPrediccion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/predict")
public class PrediccionController {
    private final PrediccionService prediccionService;

    public PrediccionController(PrediccionService prediccionService) {
        this.prediccionService = prediccionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultadoPrediccion> predecir(@PathVariable Long id) {

        ResultadoPrediccion resultado =
                prediccionService.predecirPorId(id);

        return ResponseEntity.ok(resultado);
    }
}
