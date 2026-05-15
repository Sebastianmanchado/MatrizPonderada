package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body de {@code POST /api/iniciativas/{id}/versiones}: contiene el contenido
 * completo de la nueva versión más un comentario libre que documenta el cambio.
 * Cada versión es completa y autosuficiente.
 */
public record CrearVersionIniciativaRequest(
        // "Nombre de iniciativa"
        @NotBlank @Size(min = 3, max = 200) String titulo,
        // "Area solicitante"
        @NotBlank @Size(min = 2, max = 150) String areaSolicitante,
        // "Nombre y cargo"
        @NotBlank @Size(min = 2, max = 150) String responsable,
        // "Sponsor ejecutivo"
        @NotBlank @Size(min = 2, max = 150) String sponsorEjecutivo,
        // "1. Problema o oportunidad"
        @NotBlank @Size(min = 10, max = 2000) String descripcionProblema,
        // "2. Quien lo va a usar y para que"
        @NotBlank @Size(min = 10, max = 2000) String quienUsaYPara,
        // "3. valor que genera"
        @NotBlank @Size(min = 5, max = 2000) String impactoEsperado,
        // "4. Información disponible"
        @NotBlank @Size(min = 5, max = 2000) String datosDisponibles,
        // "5. Cómo se hace hoy"
        @NotBlank @Size(min = 5, max = 2000) String comoSeHaceHoy,
        // "6. Lo que hay que saber antes de avanzar"
        @NotBlank @Size(min = 5, max = 2000) String loQueHaySaber,
        // Desplegable "Tiempo estimado para ver ese resultado". Opcional.
        @Size(max = 50) String tiempoEstimado,
        // Desplegable "¿Esa información está en un sistema accesible?". Opcional.
        @Size(max = 100) String informacionAccesible,
        @NotBlank @Size(min = 2, max = 150) String usuarioVersion,
        @Size(max = 1000) String comentarioVersion
) {}
