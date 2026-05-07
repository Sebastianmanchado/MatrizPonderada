package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "evaluacion_veto",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_evaluacion_veto",
                columnNames = {"id_evaluacion", "id_matriz_veto"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionVeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_matriz_veto", nullable = false)
    private MatrizVeto veto;

    @Column(nullable = false)
    private Boolean aplica;
}
