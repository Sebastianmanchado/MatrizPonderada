package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatrizVetoRequest(
        @NotBlank @Size(min = 5, max = 1000) String descripcion,
        @NotNull Integer orden
) {}
