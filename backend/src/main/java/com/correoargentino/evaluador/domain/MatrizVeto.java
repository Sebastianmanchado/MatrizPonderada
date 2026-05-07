package com.correoargentino.evaluador.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "matriz_veto",
        uniqueConstraints = @UniqueConstraint(name = "uq_matriz_veto_orden", columnNames = {"id_matriz", "orden"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrizVeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_matriz", nullable = false)
    private Matriz matriz;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Integer orden;
}
