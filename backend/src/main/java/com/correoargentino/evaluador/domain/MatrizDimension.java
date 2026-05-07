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
        name = "matriz_dimension",
        uniqueConstraints = @UniqueConstraint(name = "uq_matriz_dim_orden", columnNames = {"id_matriz", "orden"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrizDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_matriz", nullable = false)
    private Matriz matriz;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal peso;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private Boolean invertida;
}
