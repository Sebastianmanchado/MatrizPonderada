package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.*;
import com.correoargentino.evaluador.dto.*;

import java.math.BigDecimal;
import java.util.List;

final class Mapper {

    private Mapper() {}

    static IniciativaResponse toIniciativaResponse(Iniciativa i, BigDecimal scoreMasReciente) {
        return new IniciativaResponse(
                i.getId(),
                i.getTitulo(),
                i.getDescripcionProblema(),
                i.getDescripcionSolucion(),
                i.getAreaSolicitante(),
                i.getResponsable(),
                i.getSponsorEjecutivo(),
                i.getImpactoEsperado(),
                i.getDatosDisponibles(),
                i.getEstado(),
                i.getFechaCreacion(),
                i.getUsuarioCreador(),
                scoreMasReciente
        );
    }

    static IniciativaDetalleResponse toIniciativaDetalle(Iniciativa i, List<EvaluacionResumen> evals) {
        return new IniciativaDetalleResponse(
                i.getId(),
                i.getTitulo(),
                i.getDescripcionProblema(),
                i.getDescripcionSolucion(),
                i.getAreaSolicitante(),
                i.getResponsable(),
                i.getSponsorEjecutivo(),
                i.getImpactoEsperado(),
                i.getDatosDisponibles(),
                i.getEstado(),
                i.getFechaCreacion(),
                i.getUsuarioCreador(),
                evals
        );
    }

    static MatrizDimensionResponse toDimensionResponse(MatrizDimension d) {
        return new MatrizDimensionResponse(d.getId(), d.getNombre(), d.getDescripcion(), d.getPeso(), d.getOrden(), d.getInvertida());
    }

    static MatrizVetoResponse toVetoResponse(MatrizVeto v) {
        return new MatrizVetoResponse(v.getId(), v.getDescripcion(), v.getOrden());
    }

    static MatrizResponse toMatrizResponse(Matriz m, boolean tieneEvaluaciones) {
        return new MatrizResponse(
                m.getId(),
                m.getNombre(),
                m.getDescripcion(),
                m.getActiva(),
                m.getFechaCreacion(),
                m.getUsuarioCreador(),
                tieneEvaluaciones,
                m.getDimensiones().stream().map(Mapper::toDimensionResponse).toList(),
                m.getVetos().stream().map(Mapper::toVetoResponse).toList()
        );
    }

    static EvaluacionResumen toEvaluacionResumen(Evaluacion e) {
        return new EvaluacionResumen(
                e.getId(),
                e.getFechaEvaluacion(),
                e.getUsuarioEvaluador(),
                e.getMatriz().getId(),
                e.getMatriz().getNombre(),
                e.getPuntajeTotal(),
                e.getArquetipo(),
                e.getResultado(),
                e.getTieneVeto()
        );
    }

    static EvaluacionScoreResponse toScoreResponse(EvaluacionScore s) {
        MatrizDimension d = s.getDimension();
        return new EvaluacionScoreResponse(
                s.getId(),
                d.getId(),
                d.getNombre(),
                d.getPeso(),
                d.getInvertida(),
                s.getScore(),
                s.getPuntajePonderado()
        );
    }

    static EvaluacionVetoResponse toEvalVetoResponse(EvaluacionVeto v) {
        return new EvaluacionVetoResponse(
                v.getId(),
                v.getVeto().getId(),
                v.getVeto().getDescripcion(),
                v.getAplica()
        );
    }

    static EvaluacionDetalleResponse toEvaluacionDetalle(Evaluacion e, boolean matrizTieneEvals) {
        return new EvaluacionDetalleResponse(
                e.getId(),
                e.getIniciativa().getId(),
                e.getIniciativa().getTitulo(),
                e.getFechaEvaluacion(),
                e.getUsuarioEvaluador(),
                e.getPuntajeTotal(),
                e.getArquetipo(),
                e.getResultado(),
                e.getTieneVeto(),
                e.getNotas(),
                toMatrizResponse(e.getMatriz(), matrizTieneEvals),
                e.getScores().stream().map(Mapper::toScoreResponse).toList(),
                e.getVetos().stream().map(Mapper::toEvalVetoResponse).toList()
        );
    }
}
