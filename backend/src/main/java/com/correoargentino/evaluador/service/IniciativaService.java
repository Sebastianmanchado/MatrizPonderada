package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.Evaluacion;
import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.domain.Iniciativa;
import com.correoargentino.evaluador.dto.IniciativaDetalleResponse;
import com.correoargentino.evaluador.dto.IniciativaRequest;
import com.correoargentino.evaluador.dto.IniciativaResponse;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.IniciativaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class IniciativaService {

    private final IniciativaRepository iniciativaRepository;
    private final EvaluacionRepository evaluacionRepository;

    public IniciativaService(IniciativaRepository iniciativaRepository, EvaluacionRepository evaluacionRepository) {
        this.iniciativaRepository = iniciativaRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Transactional
    public IniciativaResponse crear(IniciativaRequest req) {
        Iniciativa i = Iniciativa.builder()
                .titulo(req.titulo())
                .descripcionProblema(req.descripcionProblema())
                .descripcionSolucion(req.descripcionSolucion())
                .areaSolicitante(req.areaSolicitante())
                .responsable(req.responsable())
                .sponsorEjecutivo(req.sponsorEjecutivo())
                .impactoEsperado(req.impactoEsperado())
                .datosDisponibles(req.datosDisponibles())
                .estado(EstadoIniciativa.SIN_EVALUAR)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador(req.usuarioCreador())
                .build();
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
        return Mapper.toIniciativaDetalle(i, evaluaciones);
    }

    @Transactional
    public IniciativaResponse actualizar(Long id, IniciativaRequest req) {
        Iniciativa i = iniciativaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Iniciativa no encontrada: " + id));
        i.setTitulo(req.titulo());
        i.setDescripcionProblema(req.descripcionProblema());
        i.setDescripcionSolucion(req.descripcionSolucion());
        i.setAreaSolicitante(req.areaSolicitante());
        i.setResponsable(req.responsable());
        i.setSponsorEjecutivo(req.sponsorEjecutivo());
        i.setImpactoEsperado(req.impactoEsperado());
        i.setDatosDisponibles(req.datosDisponibles());
        return Mapper.toIniciativaResponse(i, scoreMasReciente(id));
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
