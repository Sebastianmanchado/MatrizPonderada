package com.correoargentino.evaluador.dto;

public record EvaluacionVetoResponse(
        Long id,
        Long idMatrizVeto,
        String descripcionVeto,
        Boolean aplica
) {}
