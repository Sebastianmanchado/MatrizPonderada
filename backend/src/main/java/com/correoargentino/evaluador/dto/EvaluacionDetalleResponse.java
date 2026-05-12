package com.correoargentino.evaluador.dto;

import com.correoargentino.evaluador.domain.Arquetipo;
import com.correoargentino.evaluador.domain.Resultado;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record EvaluacionDetalleResponse(
        Long id,
        Long idIniciativa,
        String tituloIniciativa,
        OffsetDateTime fechaEvaluacion,
        String usuarioEvaluador,
        BigDecimal puntajeTotal,
        Arquetipo arquetipo,
        Resultado resultado,
        Boolean tieneVeto,
        String notas,
        IniciativaVersionResponse iniciativaVersion,
        MatrizResponse matriz,
        List<EvaluacionScoreResponse> scores,
        List<EvaluacionVetoResponse> vetos
) {}
