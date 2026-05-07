package com.correoargentino.evaluador.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MatrizResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activa,
        OffsetDateTime fechaCreacion,
        String usuarioCreador,
        Boolean tieneEvaluaciones,
        List<MatrizDimensionResponse> dimensiones,
        List<MatrizVetoResponse> vetos
) {}
