package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_iniciativa", nullable = false)
    private Iniciativa iniciativa;

    /**
     * Versión exacta de la iniciativa contra la que se realizó la evaluación.
     * El número de versión, el título y los demás campos quedan congelados acá
     * para que el historial sea consistente aunque la iniciativa siga evolucionando.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_iniciativa_version", nullable = false)
    private IniciativaVersion iniciativaVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_matriz", nullable = false)
    private Matriz matriz;

    @Column(name = "usuario_evaluador", nullable = false, length = 150)
    private String usuarioEvaluador;

    @Column(name = "fecha_evaluacion", nullable = false)
    private OffsetDateTime fechaEvaluacion;

    @Column(name = "puntaje_total", nullable = false, precision = 6, scale = 2)
    private BigDecimal puntajeTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Arquetipo arquetipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Resultado resultado;

    @Column(name = "tiene_veto", nullable = false)
    private Boolean tieneVeto;

    @Column(length = 2000)
    private String notas;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EvaluacionScore> scores = new ArrayList<>();

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EvaluacionVeto> vetos = new ArrayList<>();

    public void addScore(EvaluacionScore s) {
        s.setEvaluacion(this);
        this.scores.add(s);
    }

    public void addVeto(EvaluacionVeto v) {
        v.setEvaluacion(this);
        this.vetos.add(v);
    }
}
