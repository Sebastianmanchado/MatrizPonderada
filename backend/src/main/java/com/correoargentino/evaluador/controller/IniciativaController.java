package com.correoargentino.evaluador.controller;

import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.dto.IniciativaDetalleResponse;
import com.correoargentino.evaluador.dto.IniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaResponse;
import com.correoargentino.evaluador.service.IniciativaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/iniciativas")
public class IniciativaController {

    private final IniciativaService service;

    public IniciativaController(IniciativaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<IniciativaResponse> crear(@Valid @RequestBody IniciativaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @GetMapping
    public List<IniciativaResponse> listar(
            @RequestParam(required = false) EstadoIniciativa estado,
            @RequestParam(required = false) String search) {
        return service.listar(estado, search);
    }

    @GetMapping("/{id}")
    public IniciativaDetalleResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public IniciativaResponse actualizar(@PathVariable Long id, @Valid @RequestBody IniciativaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
