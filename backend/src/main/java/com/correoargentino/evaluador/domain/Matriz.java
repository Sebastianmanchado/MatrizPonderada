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
@Table(name = "matriz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matriz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "usuario_creador", nullable = false, length = 150)
    private String usuarioCreador;

    @OneToMany(mappedBy = "matriz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<MatrizDimension> dimensiones = new ArrayList<>();

    @OneToMany(mappedBy = "matriz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<MatrizVeto> vetos = new ArrayList<>();

    public void addDimension(MatrizDimension d) {
        d.setMatriz(this);
        this.dimensiones.add(d);
    }

    public void addVeto(MatrizVeto v) {
        v.setMatriz(this);
        this.vetos.add(v);
    }
}
