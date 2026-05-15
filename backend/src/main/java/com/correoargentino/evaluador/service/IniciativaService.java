package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.Evaluacion;
import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.domain.Iniciativa;
import com.correoargentino.evaluador.domain.IniciativaVersion;
import com.correoargentino.evaluador.dto.CrearVersionIniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaDetalleResponse;
import com.correoargentino.evaluador.dto.IniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaResponse;
import com.correoargentino.evaluador.dto.IniciativaVersionResponse;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.IniciativaRepository;
import com.correoargentino.evaluador.repository.IniciativaVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class IniciativaService {

    private final IniciativaRepository iniciativaRepository;
    private final IniciativaVersionRepository versionRepository;
    private final EvaluacionRepository evaluacionRepository;

    public IniciativaService(IniciativaRepository iniciativaRepository,
                             IniciativaVersionRepository versionRepository,
                             EvaluacionRepository evaluacionRepository) {
        this.iniciativaRepository = iniciativaRepository;
        this.versionRepository = versionRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    /**
     * Crea una iniciativa nueva. Internamente también crea la versión 1 con
     * el contenido provisto, así toda iniciativa tiene historial desde el día 0.
     */
    /**
     * Valor de relleno para la columna {@code descripcion_solucion}, que sigue
     * siendo NOT NULL en la DB pero ya no se pide en el formulario.
     */
    private static final String DESCRIPCION_SOLUCION_LEGACY = "";

    @Transactional
    public IniciativaResponse crear(IniciativaRequest req) {
        OffsetDateTime ahora = OffsetDateTime.now();
        Iniciativa i = Iniciativa.builder()
                .titulo(req.titulo())
                .descripcionProblema(req.descripcionProblema())
                .descripcionSolucion(DESCRIPCION_SOLUCION_LEGACY)
                .areaSolicitante(req.areaSolicitante())
                .responsable(req.responsable())
                .sponsorEjecutivo(req.sponsorEjecutivo())
                .impactoEsperado(req.impactoEsperado())
                .datosDisponibles(req.datosDisponibles())
                .quienUsaYPara(req.quienUsaYPara())
                .comoSeHaceHoy(req.comoSeHaceHoy())
                .loQueHaySaber(req.loQueHaySaber())
                .tiempoEstimado(req.tiempoEstimado())
                .informacionAccesible(req.informacionAccesible())
                .estado(EstadoIniciativa.SIN_EVALUAR)
                .fechaCreacion(ahora)
                .usuarioCreador(req.usuarioCreador())
                .numeroVersionActual(1)
                .build();

        // Snapshot v1 (mismo contenido que la iniciativa recién creada).
        i.addVersion(IniciativaVersion.builder()
                .numeroVersion(1)
                .titulo(req.titulo())
                .descripcionProblema(req.descripcionProblema())
                .descripcionSolucion(DESCRIPCION_SOLUCION_LEGACY)
                .areaSolicitante(req.areaSolicitante())
                .responsable(req.responsable())
                .sponsorEjecutivo(req.sponsorEjecutivo())
                .impactoEsperado(req.impactoEsperado())
                .datosDisponibles(req.datosDisponibles())
                .quienUsaYPara(req.quienUsaYPara())
                .comoSeHaceHoy(req.comoSeHaceHoy())
                .loQueHaySaber(req.loQueHaySaber())
                .tiempoEstimado(req.tiempoEstimado())
                .informacionAccesible(req.informacionAccesible())
                .usuarioVersion(req.usuarioCreador())
                .fechaVersion(ahora)
                .comentarioVersion("Versión inicial")
                .build());

        Iniciativa guardada = iniciativaRepository.save(i);
        return Mapper.toIniciativaResponse(guardada, null);
    }

    @Transactional(readOnly = true)
    public List<IniciativaResponse> listar(EstadoIniciativa estado, String search) {
        List<Iniciativa> iniciativas = iniciativaRepository.findAll(IniciativaRepository.filtrar(estado, search));
        return iniciativas.stream()
                .map(i -> Mapper.toIniciativaResponse(i, scoreMasReciente(i.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public IniciativaDetalleResponse obtener(Long id) {
        Iniciativa i = iniciativaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Iniciativa no encontrada: " + id));
        var evaluaciones = evaluacionRepository.findByIniciativaIdOrderByFechaEvaluacionDesc(id)
                .stream()
                .map(Mapper::toEvaluacionResumen)
                .toList();
        var versiones = versionRepository.findByIniciativaIdOrderByNumeroVersionDesc(id).stream()
                .map(v -> Mapper.toVersionResponse(v, i.getNumeroVersionActual()))
                .toList();
        return Mapper.toIniciativaDetalle(i, versiones, evaluaciones);
    }

    /**
     * Crea una nueva versión inmutable de la iniciativa. Hace todo en una sola transacción:
     *   - inserta la fila en {@code iniciativa_version} con el número siguiente
     *   - actualiza la iniciativa para reflejar el contenido de la nueva versión
     *   - incrementa {@code numeroVersionActual}
     *
     * Las evaluaciones existentes siguen apuntando a la versión que evaluaron, así
     * el historial de evaluación queda consistente.
     */
    @Transactional
    public IniciativaVersionResponse crearNuevaVersion(Long idIniciativa, CrearVersionIniciativaRequest req) {
        Iniciativa i = iniciativaRepository.findById(idIniciativa)
                .orElseThrow(() -> new NotFoundException("Iniciativa no encontrada: " + idIniciativa));

        int nuevoNumero = i.getNumeroVersionActual() + 1;
        OffsetDateTime ahora = OffsetDateTime.now();

        IniciativaVersion nueva = IniciativaVersion.builder()
                .iniciativa(i)
                .numeroVersion(nuevoNumero)
                .titulo(req.titulo())
                .descripcionProblema(req.descripcionProblema())
                .descripcionSolucion(DESCRIPCION_SOLUCION_LEGACY)
                .areaSolicitante(req.areaSolicitante())
                .responsable(req.responsable())
                .sponsorEjecutivo(req.sponsorEjecutivo())
                .impactoEsperado(req.impactoEsperado())
                .datosDisponibles(req.datosDisponibles())
                .quienUsaYPara(req.quienUsaYPara())
                .comoSeHaceHoy(req.comoSeHaceHoy())
                .loQueHaySaber(req.loQueHaySaber())
                .tiempoEstimado(req.tiempoEstimado())
                .informacionAccesible(req.informacionAccesible())
                .usuarioVersion(req.usuarioVersion())
                .fechaVersion(ahora)
                .comentarioVersion(req.comentarioVersion())
                .build();
        IniciativaVersion guardada = versionRepository.save(nueva);

        // Reflejar el contenido vigente en la iniciativa.
        i.setTitulo(req.titulo());
        i.setDescripcionProblema(req.descripcionProblema());
        i.setAreaSolicitante(req.areaSolicitante());
        i.setResponsable(req.responsable());
        i.setSponsorEjecutivo(req.sponsorEjecutivo());
        i.setImpactoEsperado(req.impactoEsperado());
        i.setDatosDisponibles(req.datosDisponibles());
        i.setQuienUsaYPara(req.quienUsaYPara());
        i.setComoSeHaceHoy(req.comoSeHaceHoy());
        i.setLoQueHaySaber(req.loQueHaySaber());
        i.setTiempoEstimado(req.tiempoEstimado());
        i.setInformacionAccesible(req.informacionAccesible());
        i.setNumeroVersionActual(nuevoNumero);

        return Mapper.toVersionResponse(guardada, nuevoNumero);
    }

    @Transactional(readOnly = true)
    public List<IniciativaVersionResponse> listarVersiones(Long idIniciativa) {
        Iniciativa i = iniciativaRepository.findById(idIniciativa)
                .orElseThrow(() -> new NotFoundException("Iniciativa no encontrada: " + idIniciativa));
        return versionRepository.findByIniciativaIdOrderByNumeroVersionDesc(idIniciativa).stream()
                .map(v -> Mapper.toVersionResponse(v, i.getNumeroVersionActual()))
                .toList();
    }

    @Transactional
    public void eliminar(Long id) {
        if (!iniciativaRepository.existsById(id)) {
            throw new NotFoundException("Iniciativa no encontrada: " + id);
        }
        iniciativaRepository.deleteById(id);
    }

    private BigDecimal scoreMasReciente(Long idIniciativa) {
        return evaluacionRepository.findByIniciativaIdOrderByFechaEvaluacionDesc(idIniciativa)
                .stream()
                .findFirst()
                .map(Evaluacion::getPuntajeTotal)
                .orElse(null);
    }
}
