package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /** "1. Problema o oportunidad" en la UI. */
    @Column(name = "descripcion_problema", nullable = false, length = 2000)
    private String descripcionProblema;

    /**
     * Campo histórico ("descripción de la solución") que ya no se pide en la UI.
     * Se mantiene la columna por compatibilidad con datos existentes; el service
     * pasa "" al crear iniciativas nuevas para satisfacer el NOT NULL en DB.
     */
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

    /**
     * Tiempo estimado para ver el resultado de la iniciativa. Una de las
     * opciones del desplegable del intake: "Menos de 3 meses", "3 a 6 meses",
     * "6 a 12 meses", "Mas de 12 meses", "No lo sé".
     */
    @Column(name = "tiempo_estimado", length = 50)
    private String tiempoEstimado;

    /**
     * Accesibilidad de la información que respalda la iniciativa. Una de las
     * opciones: "Sí, accesible digitalmente", "Existe pero es difícil de
     * acceder", "Existe en papel o planillas sueltas", "No sé si existe",
     * "No existe".
     */
    @Column(name = "informacion_accesible", length = 100)
    private String informacionAccesible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoIniciativa estado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "usuario_creador", nullable = false, length = 150)
    private String usuarioCreador;

    /**
     * Número de la versión vigente. Los campos {@code titulo}, {@code descripcionProblema},
     * etc. de esta entidad reflejan siempre el contenido de esta versión. El historial
     * completo vive en {@link IniciativaVersion}.
     */
    @Column(name = "numero_version_actual", nullable = false)
    @Builder.Default
    private Integer numeroVersionActual = 1;

    @OneToMany(mappedBy = "iniciativa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numeroVersion ASC")
    @Builder.Default
    private List<IniciativaVersion> versiones = new ArrayList<>();

    public void addVersion(IniciativaVersion v) {
        v.setIniciativa(this);
        this.versiones.add(v);
    }
}
