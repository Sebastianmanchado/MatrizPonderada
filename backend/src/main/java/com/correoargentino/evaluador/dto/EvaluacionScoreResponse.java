package com.correoargentino.evaluador.dto;

import java.math.BigDecimal;

public record EvaluacionScoreResponse(
        Long id,
        Long idMatrizDimension,
        String nombreDimension,
        BigDecimal pesoDimension,
        Boolean dimensionInvertida,
        Integer score,
        BigDecimal puntajePonderado
) {}
