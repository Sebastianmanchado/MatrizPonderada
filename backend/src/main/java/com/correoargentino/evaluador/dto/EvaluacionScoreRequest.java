package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvaluacionScoreRequest(
        @NotNull Long idMatrizDimension,
        @NotNull @Min(1) @Max(5) Integer score
) {}
