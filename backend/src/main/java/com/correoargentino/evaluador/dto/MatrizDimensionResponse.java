package com.correoargentino.evaluador.dto;

import java.math.BigDecimal;

public record MatrizDimensionResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal peso,
        Integer orden,
        Boolean invertida
) {}
