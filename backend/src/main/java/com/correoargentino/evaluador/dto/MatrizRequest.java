package com.correoargentino.evaluador.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MatrizRequest(
        @NotBlank @Size(min = 3, max = 200) String nombre,
        @Size(max = 1000) String descripcion,
        @NotBlank @Size(min = 2, max = 150) String usuarioCreador,
        @NotEmpty @Valid List<MatrizDimensionRequest> dimensiones,
        @Valid List<MatrizVetoRequest> vetos
) {}
