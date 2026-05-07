package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.NotNull;

public record EvaluacionVetoRequest(
        @NotNull Long idMatrizVeto,
        @NotNull Boolean aplica
) {}
