package com.correoargentino.evaluador.controller;

import com.correoargentino.evaluador.dto.MatrizRequest;
import com.correoargentino.evaluador.dto.MatrizResponse;
import com.correoargentino.evaluador.service.MatrizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matrices")
public class MatrizController {

    private final MatrizService service;

    public MatrizController(MatrizService service) {
        this.service = service;
    }

    @GetMapping
    public List<MatrizResponse> listar() {
        return service.listarActivas();
    }

    @GetMapping("/{id}")
    public MatrizResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<MatrizResponse> crear(@Valid @RequestBody MatrizRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}")
    public MatrizResponse actualizar(@PathVariable Long id, @Valid @RequestBody MatrizRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        service.desactivar(id);
    }
}
