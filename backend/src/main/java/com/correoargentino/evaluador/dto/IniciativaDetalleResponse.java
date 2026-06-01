package com.correoargentino.evaluador.dto;

import com.correoargentino.evaluador.domain.EstadoIniciativa;

import java.time.OffsetDateTime;
import java.util.List;

public record IniciativaDetalleResponse(
        Long id,
        String titulo,
        String areaSolicitante,
        String responsable,
        String sponsorEjecutivo,
        // "1. Problema o oportunidad"
        String descripcionProblema,
        // "2. Quien lo va a usar y para que"
        String quienUsaYPara,
        // "3. valor que genera"
        String impactoEsperado,
        // "4. Información disponible"
        String datosDisponibles,
        // "5. Cómo se hace hoy"
        String comoSeHaceHoy,
        // "6. Lo que hay que saber antes de avanzar"
        String loQueHaySaber,
        String tiempoEstimado,
        String informacionAccesible,
        EstadoIniciativa estado,
        OffsetDateTime fechaCreacion,
        String usuarioCreador,
        Integer numeroVersionActual,
        List<IniciativaVersionResponse> versiones,
        List<EvaluacionResumen> evaluaciones
) {}
