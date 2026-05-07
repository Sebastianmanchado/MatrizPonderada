package com.correoargentino.evaluador.dto;

import com.correoargentino.evaluador.domain.Arquetipo;
import com.correoargentino.evaluador.domain.Resultado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record EvaluacionResumen(
        Long id,
        OffsetDateTime fechaEvaluacion,
        String usuarioEvaluador,
        Long idMatriz,
        String nombreMatriz,
        BigDecimal puntajeTotal,
        Arquetipo arquetipo,
        Resultado resultado,
        Boolean tieneVeto
) {}
