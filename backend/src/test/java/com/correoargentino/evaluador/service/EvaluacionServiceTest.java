package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.*;
import com.correoargentino.evaluador.dto.EvaluacionRequest;
import com.correoargentino.evaluador.dto.EvaluacionScoreRequest;
import com.correoargentino.evaluador.dto.EvaluacionVetoRequest;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.exception.ValidationException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.IniciativaRepository;
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
class EvaluacionServiceTest {

    @Mock EvaluacionRepository evaluacionRepository;
    @Mock IniciativaRepository iniciativaRepository;
    @Mock MatrizRepository matrizRepository;

    @InjectMocks EvaluacionService service;

    private Iniciativa iniciativa;
    private Matriz matriz;
    private MatrizDimension dImpacto;
    private MatrizDimension dDatos;
    private MatrizDimension dEsfuerzo;
    private MatrizVeto veto1;

    @BeforeEach
    void setUp() {
        iniciativa = Iniciativa.builder()
                .id(1L)
                .titulo("Test")
                .descripcionProblema("p").descripcionSolucion("s")
                .areaSolicitante("a").responsable("r").sponsorEjecutivo("sp")
                .impactoEsperado("ie").datosDisponibles("dd")
                .estado(EstadoIniciativa.SIN_EVALUAR)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador("u")
                .build();

        matriz = Matriz.builder()
                .id(10L).nombre("M").activa(true)
                .fechaCreacion(OffsetDateTime.now()).usuarioCreador("u")
                .build();

        dImpacto  = MatrizDimension.builder().id(101L).matriz(matriz).nombre("Impacto").peso(new BigDecimal("0.5000")).orden(1).invertida(false).build();
        dDatos    = MatrizDimension.builder().id(102L).matriz(matriz).nombre("Datos").peso(new BigDecimal("0.3000")).orden(2).invertida(false).build();
        dEsfuerzo = MatrizDimension.builder().id(103L).matriz(matriz).nombre("Esfuerzo").peso(new BigDecimal("0.2000")).orden(3).invertida(true).build();
        matriz.getDimensiones().add(dImpacto);
        matriz.getDimensiones().add(dDatos);
        matriz.getDimensiones().add(dEsfuerzo);

        veto1 = MatrizVeto.builder().id(201L).matriz(matriz).descripcion("sin sponsor").orden(1).build();
        matriz.getVetos().add(veto1);
    }

    private EvaluacionRequest reqCon(int impacto, int datos, int esfuerzo, boolean vetoAplica) {
        return new EvaluacionRequest(
                10L,
                "evaluador",
                List.of(
                        new EvaluacionScoreRequest(101L, impacto),
                        new EvaluacionScoreRequest(102L, datos),
                        new EvaluacionScoreRequest(103L, esfuerzo)
                ),
                List.of(new EvaluacionVetoRequest(201L, vetoAplica)),
                "notas"
        );
    }

    private void mockOk() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.of(matriz));
        when(evaluacionRepository.save(any(Evaluacion.class))).thenAnswer(inv -> {
            Evaluacion e = inv.getArgument(0);
            e.setId(500L);
            return e;
        });
        when(evaluacionRepository.existsByMatrizId(10L)).thenReturn(true);
    }

    @Test
    void crear_scoresPerfectos_devuelveAvanzaQuickWin() {
        mockOk();
        // 5*0.5 + 5*0.3 + 5*0.2 = 5.0
        var resp = service.crear(1L, reqCon(5, 5, 5, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("5.00");
        assertThat(resp.resultado()).isEqualTo(Resultado.AVANZA);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.QUICK_WIN);
        assertThat(resp.tieneVeto()).isFalse();
        assertThat(iniciativa.getEstado()).isEqualTo(EstadoIniciativa.APROBADO);
    }

    @Test
    void crear_puntajeAltoEsfuerzoBajo_majorProject() {
        mockOk();
        // 5*0.5 + 5*0.3 + 1*0.2 = 4.0 → AVANZA, esfuerzo=1 < 3 → MAJOR_PROJECT
        var resp = service.crear(1L, reqCon(5, 5, 1, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("4.00");
        assertThat(resp.resultado()).isEqualTo(Resultado.AVANZA);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.MAJOR_PROJECT);
        assertThat(iniciativa.getEstado()).isEqualTo(EstadoIniciativa.APROBADO);
    }

    @Test
    void crear_puntajeMedio_revisar() {
        mockOk();
        // 3*0.5 + 3*0.3 + 3*0.2 = 3.0 → REVISAR
        var resp = service.crear(1L, reqCon(3, 3, 3, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("3.00");
        assertThat(resp.resultado()).isEqualTo(Resultado.REVISAR);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.A_REVISAR);
        assertThat(iniciativa.getEstado()).isEqualTo(EstadoIniciativa.A_REVISAR);
    }

    @Test
    void crear_puntajeBajo_descartarTimeWaster() {
        mockOk();
        // 1*0.5 + 1*0.3 + 1*0.2 = 1.0 → DESCARTAR + TIME_WASTER
        var resp = service.crear(1L, reqCon(1, 1, 1, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("1.00");
        assertThat(resp.resultado()).isEqualTo(Resultado.DESCARTAR);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.TIME_WASTER);
        assertThat(iniciativa.getEstado()).isEqualTo(EstadoIniciativa.RECHAZADO);
    }

    @Test
    void crear_conVeto_overrideADescartarYVetado() {
        mockOk();
        // 5*0.5 + 5*0.3 + 5*0.2 = 5.0 (alto), pero veto activo
        var resp = service.crear(1L, reqCon(5, 5, 5, true));

        assertThat(resp.tieneVeto()).isTrue();
        assertThat(resp.resultado()).isEqualTo(Resultado.DESCARTAR);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.A_REVISAR);
        assertThat(iniciativa.getEstado()).isEqualTo(EstadoIniciativa.VETADO);
    }

    @Test
    void crear_umbralExacto_avanza() {
        mockOk();
        // 4*0.5 + 3*0.3 + 4*0.2 = 2.0 + 0.9 + 0.8 = 3.7 → AVANZA
        var resp = service.crear(1L, reqCon(4, 3, 4, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("3.70");
        assertThat(resp.resultado()).isEqualTo(Resultado.AVANZA);
        assertThat(resp.arquetipo()).isEqualTo(Arquetipo.QUICK_WIN);
    }

    @Test
    void crear_umbralExactoRevisar_25() {
        mockOk();
        // 2*0.5 + 3*0.3 + 3*0.2 = 1.0 + 0.9 + 0.6 = 2.5 → REVISAR
        var resp = service.crear(1L, reqCon(2, 3, 3, false));

        assertThat(resp.puntajeTotal()).isEqualByComparingTo("2.50");
        assertThat(resp.resultado()).isEqualTo(Resultado.REVISAR);
    }

    @Test
    void crear_iniciativaInexistente_lanzaNotFound() {
        when(iniciativaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crear(99L, reqCon(3, 3, 3, false)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void crear_matrizInexistente_lanzaNotFound() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crear(1L, reqCon(3, 3, 3, false)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void crear_scoreFueraDeRango_lanzaValidationException() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.of(matriz));
        var bad = new EvaluacionRequest(
                10L, "u",
                List.of(
                        new EvaluacionScoreRequest(101L, 7),
                        new EvaluacionScoreRequest(102L, 3),
                        new EvaluacionScoreRequest(103L, 3)
                ),
                List.of(new EvaluacionVetoRequest(201L, false)),
                null
        );
        assertThatThrownBy(() -> service.crear(1L, bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("entre 1 y 5");
    }

    @Test
    void crear_faltaScoreParaDimension_lanzaValidationException() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.of(matriz));
        var bad = new EvaluacionRequest(
                10L, "u",
                List.of(
                        new EvaluacionScoreRequest(101L, 3),
                        new EvaluacionScoreRequest(102L, 3)
                ),
                List.of(new EvaluacionVetoRequest(201L, false)),
                null
        );
        assertThatThrownBy(() -> service.crear(1L, bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("scores");
    }

    @Test
    void crear_dimensionAjenaALaMatriz_lanzaValidationException() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.of(matriz));
        var bad = new EvaluacionRequest(
                10L, "u",
                List.of(
                        new EvaluacionScoreRequest(101L, 3),
                        new EvaluacionScoreRequest(102L, 3),
                        new EvaluacionScoreRequest(999L, 3)
                ),
                List.of(new EvaluacionVetoRequest(201L, false)),
                null
        );
        assertThatThrownBy(() -> service.crear(1L, bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("no pertenece");
    }

    @Test
    void crear_faltaVetoParaMatriz_lanzaValidationException() {
        when(iniciativaRepository.findById(1L)).thenReturn(Optional.of(iniciativa));
        when(matrizRepository.findById(10L)).thenReturn(Optional.of(matriz));
        var bad = new EvaluacionRequest(
                10L, "u",
                List.of(
                        new EvaluacionScoreRequest(101L, 3),
                        new EvaluacionScoreRequest(102L, 3),
                        new EvaluacionScoreRequest(103L, 3)
                ),
                List.of(),
                null
        );
        assertThatThrownBy(() -> service.crear(1L, bad))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("vetos");
    }
}
