package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "iniciativa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Iniciativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion_problema", nullable = false, length = 2000)
    private String descripcionProblema;

    @Column(name = "descripcion_solucion", nullable = false, length = 2000)
    private String descripcionSolucion;

    @Column(name = "area_solicitante", nullable = false, length = 150)
    private String areaSolicitante;

    @Column(nullable = false, length = 150)
    private String responsable;

    @Column(name = "sponsor_ejecutivo", nullable = false, length = 150)
    private String sponsorEjecutivo;

    @Column(name = "impacto_esperado", nullable = false, length = 2000)
    private String impactoEsperado;

    @Column(name = "datos_disponibles", nullable = false, length = 2000)
    private String datosDisponibles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoIniciativa estado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "usuario_creador", nullable = false, length = 150)
    private String usuarioCreador;
}
