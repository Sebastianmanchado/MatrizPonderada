package com.correoargentino.evaluador.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EvaluacionRequest(
        @NotNull Long idMatriz,
        @NotBlank @Size(min = 2, max = 150) String usuarioEvaluador,
        @NotEmpty @Valid List<EvaluacionScoreRequest> scores,
        @NotNull @Valid List<EvaluacionVetoRequest> vetos,
        @Size(max = 2000) String notas
) {}
