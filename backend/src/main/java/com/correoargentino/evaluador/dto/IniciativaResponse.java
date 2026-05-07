package com.correoargentino.evaluador.dto;

import com.correoargentino.evaluador.domain.EstadoIniciativa;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record IniciativaResponse(
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
        BigDecimal scoreMasReciente
) {}
