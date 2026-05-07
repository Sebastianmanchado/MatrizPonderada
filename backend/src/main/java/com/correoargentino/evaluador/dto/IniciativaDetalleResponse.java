package com.correoargentino.evaluador.dto;

import com.correoargentino.evaluador.domain.EstadoIniciativa;

import java.time.OffsetDateTime;
import java.util.List;

public record IniciativaDetalleResponse(
        Long id,
        String titulo,
        String descripcionProblema,
        String descripcionSolucion,
        String areaSolicitante,
        String responsable,
        String sponsorEjecutivo,
        String impactoEsperado,
        String datosDisponibles,
        EstadoIniciativa estado,
        OffsetDateTime fechaCreacion,
        String usuarioCreador,
        List<EvaluacionResumen> evaluaciones
) {}
