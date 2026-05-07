package com.correoargentino.evaluador.controller;

import com.correoargentino.evaluador.dto.EvaluacionDetalleResponse;
import com.correoargentino.evaluador.dto.EvaluacionRequest;
import com.correoargentino.evaluador.dto.EvaluacionResumen;
import com.correoargentino.evaluador.service.EvaluacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EvaluacionController {

    private final EvaluacionService service;

    public EvaluacionController(EvaluacionService service) {
        this.service = service;
    }

    @PostMapping("/api/iniciativas/{idIniciativa}/evaluaciones")
    public ResponseEntity<EvaluacionDetalleResponse> crear(
            @PathVariable Long idIniciativa,
            @Valid @RequestBody EvaluacionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(idIniciativa, req));
    }

    @GetMapping("/api/iniciativas/{idIniciativa}/evaluaciones")
    public List<EvaluacionResumen> listarPorIniciativa(@PathVariable Long idIniciativa) {
        return service.listarPorIniciativa(idIniciativa);
    }

    @GetMapping("/api/evaluaciones/{id}")
    public EvaluacionDetalleResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
}
