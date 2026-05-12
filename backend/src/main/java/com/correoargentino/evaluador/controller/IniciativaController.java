package com.correoargentino.evaluador.controller;

import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.dto.CrearVersionIniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaDetalleResponse;
import com.correoargentino.evaluador.dto.IniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaResponse;
import com.correoargentino.evaluador.dto.IniciativaVersionResponse;
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

    /**
     * Crea una nueva versión inmutable de la iniciativa. Reemplaza al antiguo
     * {@code PUT /api/iniciativas/{id}}: toda modificación de contenido genera
     * una versión nueva. Las evaluaciones existentes mantienen el snapshot de
     * la versión que evaluaron.
     */
    @PostMapping("/{id}/versiones")
    public ResponseEntity<IniciativaVersionResponse> crearVersion(
            @PathVariable Long id,
            @Valid @RequestBody CrearVersionIniciativaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearNuevaVersion(id, req));
    }

    @GetMapping("/{id}/versiones")
    public List<IniciativaVersionResponse> listarVersiones(@PathVariable Long id) {
        return service.listarVersiones(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
