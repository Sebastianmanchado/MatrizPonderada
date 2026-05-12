package com.correoargentino.evaluador.dto;

import java.time.OffsetDateTime;

/**
 * Snapshot de una versión específica de una iniciativa.
 * Lo devuelven {@code GET /api/iniciativas/{id}/versiones} y los detalles
 * de evaluación para mostrar el contenido tal como estaba al momento
 * de evaluar.
 */
public record IniciativaVersionResponse(
        Long id,
        Long idIniciativa,
        Integer numeroVersion,
        String titulo,
        String descripcionProblema,
        String descripcionSolucion,
        String areaSolicitante,
        String responsable,
        String sponsorEjecutivo,
        String impactoEsperado,
        String datosDisponibles,
        String usuarioVersion,
        OffsetDateTime fechaVersion,
        String comentarioVersion,
        boolean esActual
) {}
