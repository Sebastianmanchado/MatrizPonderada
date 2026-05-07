package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.Matriz;
import com.correoargentino.evaluador.dto.MatrizDimensionRequest;
import com.correoargentino.evaluador.dto.MatrizRequest;
import com.correoargentino.evaluador.dto.MatrizVetoRequest;
import com.correoargentino.evaluador.exception.ConflictException;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.exception.ValidationException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.MatrizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatrizServiceTest {

    @Mock MatrizRepository matrizRepository;
    @Mock EvaluacionRepository evaluacionRepository;

    @InjectMocks MatrizService service;

    private MatrizRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new MatrizRequest(
                "Matriz test",
                "desc",
                "user",
                List.of(
                        new MatrizDimensionRequest("Impacto", "d", new BigDecimal("0.30"), 1, false),
                        new MatrizDimensionRequest("Datos",   "d", new BigDecimal("0.25"), 2, false),
                        new MatrizDimensionRequest("Esfuerzo","d", new BigDecimal("0.15"), 3, true),
                        new MatrizDimensionRequest("Tiempo",  "d", new BigDecimal("0.15"), 4, false),
                        new MatrizDimensionRequest("Riesgo",  "d", new BigDecimal("0.10"), 5, false),
                        new MatrizDimensionRequest("Alineac", "d", new BigDecimal("0.05"), 6, false)
                ),
                List.of(new MatrizVetoRequest("sin sponsor", 1))
        );
    }

    @Test
    void crear_pesoSumaUno_devuelveMatriz() {
        when(matrizRepository.save(any(Matriz.class))).thenAnswer(inv -> {
            Matriz m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        var resp = service.crear(requestValido);

        assertThat(resp.id()).isEqualTo(99L);
        assertThat(resp.dimensiones()).hasSize(6);
        assertThat(resp.vetos()).hasSize(1);
        assertThat(resp.activa()).isTrue();
    }

    @Test
    void crear_pesoNoSumaUno_lanzaValidationException() {
        var bad = new MatrizRequest(
                "x", "x", "u",
                List.of(
                        new MatrizDimensionRequest("a", "d", new BigDecimal("0.5"), 1, false),
                        new MatrizDimensionRequest("b", "d", new BigDecimal("0.4"), 2, false)
                ),
                List.of()
        );

        assertThatThrownBy(() -> service.crear(bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("suma de pesos");
    }

    @Test
    void crear_dentroDeTolerancia_aceptado() {
        var ok = new MatrizRequest(
                "x", "x", "u",
                List.of(
                        new MatrizDimensionRequest("a", "d", new BigDecimal("0.6"),    1, false),
                        new MatrizDimensionRequest("b", "d", new BigDecimal("0.4001"), 2, false)
                ),
                List.of()
        );
        when(matrizRepository.save(any(Matriz.class))).thenAnswer(inv -> {
            Matriz m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        var resp = service.crear(ok);
        assertThat(resp).isNotNull();
    }

    @Test
    void crear_ordenDimensionDuplicado_lanzaValidationException() {
        var bad = new MatrizRequest(
                "x", "x", "u",
                List.of(
                        new MatrizDimensionRequest("a", "d", new BigDecimal("0.5"), 1, false),
                        new MatrizDimensionRequest("b", "d", new BigDecimal("0.5"), 1, false)
                ),
                List.of()
        );
        assertThatThrownBy(() -> service.crear(bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("orden");
    }

    @Test
    void crear_ordenVetoDuplicado_lanzaValidationException() {
        var bad = new MatrizRequest(
                "x", "x", "u",
                List.of(new MatrizDimensionRequest("a", "d", BigDecimal.ONE, 1, false)),
                List.of(
                        new MatrizVetoRequest("v1", 1),
                        new MatrizVetoRequest("v2", 1)
                )
        );
        assertThatThrownBy(() -> service.crear(bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("vetos");
    }

    @Test
    void crear_sinDimensiones_lanzaValidationException() {
        var bad = new MatrizRequest("x","x","u", List.of(), List.of());
        assertThatThrownBy(() -> service.crear(bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("al menos una dimensión");
    }

    @Test
    void actualizar_matrizConEvaluaciones_lanzaConflict() {
        Matriz existente = Matriz.builder()
                .id(7L).nombre("x").activa(true)
                .fechaCreacion(OffsetDateTime.now()).usuarioCreador("u")
                .build();
        when(matrizRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(evaluacionRepository.existsByMatrizId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(7L, requestValido))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ya fue usada");
    }

    @Test
    void actualizar_matrizSinEvaluaciones_actualiza() {
        Matriz existente = Matriz.builder()
                .id(7L).nombre("x").activa(true)
                .fechaCreacion(OffsetDateTime.now()).usuarioCreador("u")
                .build();
        when(matrizRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(evaluacionRepository.existsByMatrizId(7L)).thenReturn(false);

        var resp = service.actualizar(7L, requestValido);

        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.nombre()).isEqualTo("Matriz test");
        assertThat(resp.dimensiones()).hasSize(6);
    }

    @Test
    void actualizar_matrizInexistente_lanzaNotFound() {
        when(matrizRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, requestValido))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void desactivar_aunConEvaluaciones_permitido() {
        Matriz m = Matriz.builder().id(1L).activa(true)
                .nombre("x").fechaCreacion(OffsetDateTime.now()).usuarioCreador("u")
                .build();
        when(matrizRepository.findById(1L)).thenReturn(Optional.of(m));

        service.desactivar(1L);

        assertThat(m.getActiva()).isFalse();
    }
}
