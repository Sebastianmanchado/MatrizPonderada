package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MatrizDimensionRequest(
        @NotBlank @Size(min = 2, max = 200) String nombre,
        @Size(max = 1000) String descripcion,
        @NotNull @PositiveOrZero BigDecimal peso,
        @NotNull Integer orden,
        @NotNull Boolean invertida
) {}
