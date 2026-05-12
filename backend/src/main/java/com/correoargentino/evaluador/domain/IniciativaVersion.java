package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Snapshot inmutable del contenido de una iniciativa en un punto dado.
 * Cada vez que el responsable corrige/actualiza la iniciativa se crea
 * una nueva fila acá. Las evaluaciones referencian la versión exacta
 * con la que se trabajó, así el historial es coherente aunque la
 * iniciativa siga evolucionando.
 */
@Entity
@Table(name = "iniciativa_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciativaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_iniciativa", nullable = false)
    private Iniciativa iniciativa;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;

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

    @Column(name = "usuario_version", nullable = false, length = 150)
    private String usuarioVersion;

    @Column(name = "fecha_version", nullable = false)
    private OffsetDateTime fechaVersion;

    @Column(name = "comentario_version", length = 1000)
    private String comentarioVersion;
}
