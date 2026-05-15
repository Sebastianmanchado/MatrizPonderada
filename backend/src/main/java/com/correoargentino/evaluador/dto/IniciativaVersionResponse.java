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
        String areaSolicitante,
        String responsable,
        String sponsorEjecutivo,
        // "1. Problema o oportunidad"
        String descripcionProblema,
        // "2. Quien lo va a usar y para que"
        String quienUsaYPara,
        // "3. valor que genera"
        String impactoEsperado,
        // "4. Información disponible"
        String datosDisponibles,
        // "5. Cómo se hace hoy"
        String comoSeHaceHoy,
        // "6. Lo que hay que saber antes de avanzar"
        String loQueHaySaber,
        String tiempoEstimado,
        String informacionAccesible,
        String usuarioVersion,
        OffsetDateTime fechaVersion,
        String comentarioVersion,
        boolean esActual
) {}
