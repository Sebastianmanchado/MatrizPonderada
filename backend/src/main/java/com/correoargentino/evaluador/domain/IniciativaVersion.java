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

    /** "1. Problema o oportunidad" en la UI. */
    @Column(name = "descripcion_problema", nullable = false, length = 2000)
    private String descripcionProblema;

    /** Campo histórico que ya no se pide en la UI. Ver {@link Iniciativa}. */
    @Column(name = "descripcion_solucion", nullable = false, length = 2000)
    private String descripcionSolucion;

    @Column(name = "area_solicitante", nullable = false, length = 150)
    private String areaSolicitante;

    /** "Nombre y cargo" en la UI. */
    @Column(nullable = false, length = 150)
    private String responsable;

    @Column(name = "sponsor_ejecutivo", nullable = false, length = 150)
    private String sponsorEjecutivo;

    /** "3. valor que genera" en la UI. */
    @Column(name = "impacto_esperado", nullable = false, length = 2000)
    private String impactoEsperado;

    /** "4. Información disponible" en la UI. */
    @Column(name = "datos_disponibles", nullable = false, length = 2000)
    private String datosDisponibles;

    /** "2. Quien lo va a usar y para que". */
    @Column(name = "quien_usa_y_para", length = 2000)
    private String quienUsaYPara;

    /** "5. Cómo se hace hoy". */
    @Column(name = "como_se_hace_hoy", length = 2000)
    private String comoSeHaceHoy;

    /** "6. Lo que hay que saber antes de avanzar". */
    @Column(name = "lo_que_hay_saber", length = 2000)
    private String loQueHaySaber;

    /** Desplegable: "Tiempo estimado para ver ese resultado". */
    @Column(name = "tiempo_estimado", length = 50)
    private String tiempoEstimado;

    /** Desplegable: "¿Esa información está en un sistema accesible?". */
    @Column(name = "informacion_accesible", length = 100)
    private String informacionAccesible;

    @Column(name = "usuario_version", nullable = false, length = 150)
    private String usuarioVersion;

    @Column(name = "fecha_version", nullable = false)
    private OffsetDateTime fechaVersion;

    @Column(name = "comentario_version", length = 1000)
    private String comentarioVersion;
}
