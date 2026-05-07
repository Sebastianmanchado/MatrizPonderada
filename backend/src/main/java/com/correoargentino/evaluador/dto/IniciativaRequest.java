package com.correoargentino.evaluador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IniciativaRequest(
        @NotBlank @Size(min = 3, max = 200) String titulo,
        @NotBlank @Size(min = 10, max = 2000) String descripcionProblema,
        @NotBlank @Size(min = 10, max = 2000) String descripcionSolucion,
        @NotBlank @Size(min = 2, max = 150) String areaSolicitante,
        @NotBlank @Size(min = 2, max = 150) String responsable,
        @NotBlank @Size(min = 2, max = 150) String sponsorEjecutivo,
        @NotBlank @Size(min = 5, max = 2000) String impactoEsperado,
        @NotBlank @Size(min = 5, max = 2000) String datosDisponibles,
        @NotBlank @Size(min = 2, max = 150) String usuarioCreador
) {}
