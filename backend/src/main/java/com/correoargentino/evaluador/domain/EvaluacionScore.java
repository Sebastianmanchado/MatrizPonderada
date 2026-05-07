package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "evaluacion_score",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_evaluacion_dim",
                columnNames = {"id_evaluacion", "id_matriz_dimension"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_matriz_dimension", nullable = false)
    private MatrizDimension dimension;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "puntaje_ponderado", nullable = false, precision = 8, scale = 4)
    private BigDecimal puntajePonderado;
}
