package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.domain.Iniciativa;
import com.correoargentino.evaluador.domain.IniciativaVersion;
import com.correoargentino.evaluador.dto.CrearVersionIniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaRequest;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.IniciativaRepository;
import com.correoargentino.evaluador.repository.IniciativaVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciativaServiceTest {

    @Mock IniciativaRepository iniciativaRepository;
    @Mock IniciativaVersionRepository versionRepository;
    @Mock EvaluacionRepository evaluacionRepository;

    @InjectMocks IniciativaService service;

    private IniciativaRequest reqBase() {
        return new IniciativaRequest(
                "Asistente IA reclamos",
                "Atención al cliente",
                "Ana Pérez — Jefa de operaciones",
                "Roberto Gómez",
                "Los reclamos llegan sin clasificar y demoran.",
                "Equipo de Atención al Cliente para clasificar reclamos entrantes.",
                "Bajar TTR 30%.",
                "Tickets de 24 meses con categoría.",
                "Hoy se clasifica manualmente con SLA de 48 hs.",
                "Necesitamos confirmación legal sobre el uso de datos personales.",
                "3 a 6 meses",
                "Sí, accesible digitalmente",
                "ana.perez"
        );
    }

    private CrearVersionIniciativaRequest versionReq(String titulo, String comentario) {
        return new CrearVersionIniciativaRequest(
                titulo,
                "Atención al cliente",
                "Ana Pérez — Jefa de operaciones",
                "Roberto Gómez",
                "Reclamos sin clasificar — actualizado.",
                "Equipo de AC + Supervisores para validar la propuesta del modelo.",
                "Bajar TTR 35%.",
                "Tickets de 36 meses con categoría y sentimiento.",
                "Clasificación manual con feedback semanal.",
                "Pendiente acuerdo con seguridad informática para acceso al dataset.",
                "6 a 12 meses",
                "Existe pero es difícil de acceder",
                "ana.perez",
                comentario
        );
    }

    @Test
    void crear_iniciativa_tambienCreaVersion1() {
        when(iniciativaRepository.save(any(Iniciativa.class))).thenAnswer(inv -> {
            Iniciativa i = inv.getArgument(0);
            i.setId(7L);
            return i;
        });

        var resp = service.crear(reqBase());

        assertThat(resp.numeroVersionActual()).isEqualTo(1);

        ArgumentCaptor<Iniciativa> captor = ArgumentCaptor.forClass(Iniciativa.class);
        org.mockito.Mockito.verify(iniciativaRepository).save(captor.capture());
        Iniciativa guardada = captor.getValue();
        assertThat(guardada.getVersiones()).hasSize(1);
        IniciativaVersion v1 = guardada.getVersiones().get(0);
        assertThat(v1.getNumeroVersion()).isEqualTo(1);
        assertThat(v1.getTitulo()).isEqualTo("Asistente IA reclamos");
        assertThat(v1.getComentarioVersion()).isEqualTo("Versión inicial");
    }

    @Test
    void crearNuevaVersion_incrementaContadorYActualizaContenido() {
        Iniciativa iniciativa = Iniciativa.builder()
                .id(7L)
                .titulo("Asistente IA reclamos")
                .descripcionProblema("p").descripcionSolucion("s")
                .areaSolicitante("a").responsable("r").sponsorEjecutivo("sp")
                .impactoEsperado("ie").datosDisponibles("dd")
                .estado(EstadoIniciativa.SIN_EVALUAR)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador("ana.perez")
                .numeroVersionActual(1)
                .build();
        when(iniciativaRepository.findById(7L)).thenReturn(Optional.of(iniciativa));
        when(versionRepository.save(any(IniciativaVersion.class))).thenAnswer(inv -> {
            IniciativaVersion v = inv.getArgument(0);
            v.setId(101L);
            return v;
        });

        var resp = service.crearNuevaVersion(7L, versionReq("Asistente IA reclamos v2", "agregamos sentimiento"));

        assertThat(resp.numeroVersion()).isEqualTo(2);
        assertThat(resp.titulo()).isEqualTo("Asistente IA reclamos v2");
        assertThat(resp.comentarioVersion()).isEqualTo("agregamos sentimiento");
        assertThat(resp.esActual()).isTrue();

        // El contenido vigente de la iniciativa debe reflejar el de la nueva versión.
        assertThat(iniciativa.getNumeroVersionActual()).isEqualTo(2);
        assertThat(iniciativa.getTitulo()).isEqualTo("Asistente IA reclamos v2");
        assertThat(iniciativa.getImpactoEsperado()).isEqualTo("Bajar TTR 35%.");
    }

    @Test
    void crearNuevaVersion_iniciativaInexistente_lanzaNotFound() {
        when(iniciativaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crearNuevaVersion(99L, versionReq("x", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listarVersiones_devuelveTodasMarcandoLaActual() {
        Iniciativa iniciativa = Iniciativa.builder()
                .id(7L)
                .titulo("t").descripcionProblema("p").descripcionSolucion("s")
                .areaSolicitante("a").responsable("r").sponsorEjecutivo("sp")
                .impactoEsperado("ie").datosDisponibles("dd")
                .estado(EstadoIniciativa.SIN_EVALUAR)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador("u")
                .numeroVersionActual(2)
                .build();
        IniciativaVersion v1 = IniciativaVersion.builder()
                .id(100L).iniciativa(iniciativa).numeroVersion(1)
                .titulo("t").descripcionProblema("p").descripcionSolucion("s")
                .areaSolicitante("a").responsable("r").sponsorEjecutivo("sp")
                .impactoEsperado("ie").datosDisponibles("dd")
                .usuarioVersion("u").fechaVersion(OffsetDateTime.now())
                .build();
        IniciativaVersion v2 = IniciativaVersion.builder()
                .id(101L).iniciativa(iniciativa).numeroVersion(2)
                .titulo("t").descripcionProblema("p").descripcionSolucion("s")
                .areaSolicitante("a").responsable("r").sponsorEjecutivo("sp")
                .impactoEsperado("ie").datosDisponibles("dd")
                .usuarioVersion("u").fechaVersion(OffsetDateTime.now())
                .build();
        when(iniciativaRepository.findById(7L)).thenReturn(Optional.of(iniciativa));
        when(versionRepository.findByIniciativaIdOrderByNumeroVersionDesc(7L)).thenReturn(List.of(v2, v1));

        var versiones = service.listarVersiones(7L);

        assertThat(versiones).hasSize(2);
        assertThat(versiones.get(0).numeroVersion()).isEqualTo(2);
        assertThat(versiones.get(0).esActual()).isTrue();
        assertThat(versiones.get(1).numeroVersion()).isEqualTo(1);
        assertThat(versiones.get(1).esActual()).isFalse();
    }
}
