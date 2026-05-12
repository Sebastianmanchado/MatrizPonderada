package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.*;
import com.correoargentino.evaluador.dto.*;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.exception.ValidationException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.IniciativaRepository;
import com.correoargentino.evaluador.repository.IniciativaVersionRepository;
import com.correoargentino.evaluador.repository.MatrizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class EvaluacionService {

    static final BigDecimal UMBRAL_AVANZA = new BigDecimal("3.5");
    static final BigDecimal UMBRAL_REVISAR = new BigDecimal("2.5");
    static final int UMBRAL_ESFUERZO_QUICK = 3;

    private final EvaluacionRepository evaluacionRepository;
    private final IniciativaRepository iniciativaRepository;
    private final IniciativaVersionRepository versionRepository;
    private final MatrizRepository matrizRepository;

    public EvaluacionService(EvaluacionRepository evaluacionRepository,
                             IniciativaRepository iniciativaRepository,
                             IniciativaVersionRepository versionRepository,
                             MatrizRepository matrizRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.iniciativaRepository = iniciativaRepository;
        this.versionRepository = versionRepository;
        this.matrizRepository = matrizRepository;
    }

    @Transactional
    public EvaluacionDetalleResponse crear(Long idIniciativa, EvaluacionRequest req) {
        Iniciativa iniciativa = iniciativaRepository.findById(idIniciativa)
                .orElseThrow(() -> new NotFoundException("Iniciativa no encontrada: " + idIniciativa));

        // La evaluación se congela contra la versión actual de la iniciativa.
        // Si después se crea una v(n+1), esta evaluación sigue mostrando la v(n).
        IniciativaVersion versionActual = versionRepository
                .findByIniciativaIdAndNumeroVersion(iniciativa.getId(), iniciativa.getNumeroVersionActual())
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró la versión actual (" + iniciativa.getNumeroVersionActual()
                                + ") de la iniciativa " + iniciativa.getId()));

        Matriz matriz = matrizRepository.findById(req.idMatriz())
                .orElseThrow(() -> new NotFoundException("Matriz no encontrada: " + req.idMatriz()));

        Map<Long, MatrizDimension> dimensionesPorId = new LinkedHashMap<>();
        for (MatrizDimension d : matriz.getDimensiones()) {
            dimensionesPorId.put(d.getId(), d);
        }
        Map<Long, MatrizVeto> vetosPorId = new LinkedHashMap<>();
        for (MatrizVeto v : matriz.getVetos()) {
            vetosPorId.put(v.getId(), v);
        }

        validarScoresCompletos(req.scores(), dimensionesPorId.keySet());
        validarVetosCompletos(req.vetos(), vetosPorId.keySet());

        Evaluacion evaluacion = Evaluacion.builder()
                .iniciativa(iniciativa)
                .iniciativaVersion(versionActual)
                .matriz(matriz)
                .usuarioEvaluador(req.usuarioEvaluador())
                .fechaEvaluacion(OffsetDateTime.now())
                .notas(req.notas())
                .build();

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        for (EvaluacionScoreRequest sr : req.scores()) {
            MatrizDimension dim = dimensionesPorId.get(sr.idMatrizDimension());
            BigDecimal ponderado = BigDecimal.valueOf(sr.score())
                    .multiply(dim.getPeso())
                    .setScale(4, RoundingMode.HALF_UP);
            puntajeTotal = puntajeTotal.add(ponderado);

            evaluacion.addScore(EvaluacionScore.builder()
                    .dimension(dim)
                    .score(sr.score())
                    .puntajePonderado(ponderado)
                    .build());
        }
        puntajeTotal = puntajeTotal.setScale(2, RoundingMode.HALF_UP);

        boolean tieneVeto = false;
        for (EvaluacionVetoRequest vr : req.vetos()) {
            if (Boolean.TRUE.equals(vr.aplica())) {
                tieneVeto = true;
            }
            evaluacion.addVeto(EvaluacionVeto.builder()
                    .veto(vetosPorId.get(vr.idMatrizVeto()))
                    .aplica(vr.aplica())
                    .build());
        }

        Resultado resultado = calcularResultado(puntajeTotal, tieneVeto);
        Integer scoreEsfuerzo = encontrarScoreEsfuerzo(matriz, req.scores());
        Arquetipo arquetipo = calcularArquetipo(puntajeTotal, tieneVeto, scoreEsfuerzo);

        evaluacion.setPuntajeTotal(puntajeTotal);
        evaluacion.setTieneVeto(tieneVeto);
        evaluacion.setResultado(resultado);
        evaluacion.setArquetipo(arquetipo);

        Evaluacion guardada = evaluacionRepository.save(evaluacion);

        iniciativa.setEstado(mapearEstado(resultado, tieneVeto));

        return Mapper.toEvaluacionDetalle(guardada, true);
    }

    @Transactional(readOnly = true)
    public List<EvaluacionResumen> listarPorIniciativa(Long idIniciativa) {
        if (!iniciativaRepository.existsById(idIniciativa)) {
            throw new NotFoundException("Iniciativa no encontrada: " + idIniciativa);
        }
        return evaluacionRepository.findByIniciativaIdOrderByFechaEvaluacionDesc(idIniciativa).stream()
                .map(Mapper::toEvaluacionResumen)
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluacionDetalleResponse obtener(Long id) {
        Evaluacion e = evaluacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Evaluación no encontrada: " + id));
        return Mapper.toEvaluacionDetalle(e, evaluacionRepository.existsByMatrizId(e.getMatriz().getId()));
    }

    void validarScoresCompletos(List<EvaluacionScoreRequest> scores, Set<Long> dimensionIds) {
        if (scores.size() != dimensionIds.size()) {
            throw new ValidationException(
                    "Se esperan exactamente " + dimensionIds.size() + " scores (uno por dimensión). Recibidos: " + scores.size()
            );
        }
        Set<Long> recibidas = new HashSet<>();
        for (EvaluacionScoreRequest sr : scores) {
            if (!dimensionIds.contains(sr.idMatrizDimension())) {
                throw new ValidationException("Dimensión no pertenece a la matriz: " + sr.idMatrizDimension());
            }
            if (!recibidas.add(sr.idMatrizDimension())) {
                throw new ValidationException("Dimensión duplicada en los scores: " + sr.idMatrizDimension());
            }
            if (sr.score() < 1 || sr.score() > 5) {
                throw new ValidationException("El score debe estar entre 1 y 5. Recibido: " + sr.score());
            }
        }
    }

    void validarVetosCompletos(List<EvaluacionVetoRequest> vetosReq, Set<Long> vetoIds) {
        if (vetosReq.size() != vetoIds.size()) {
            throw new ValidationException(
                    "Se esperan exactamente " + vetoIds.size() + " vetos (uno por veto de la matriz). Recibidos: " + vetosReq.size()
            );
        }
        Set<Long> recibidos = new HashSet<>();
        for (EvaluacionVetoRequest vr : vetosReq) {
            if (!vetoIds.contains(vr.idMatrizVeto())) {
                throw new ValidationException("Veto no pertenece a la matriz: " + vr.idMatrizVeto());
            }
            if (!recibidos.add(vr.idMatrizVeto())) {
                throw new ValidationException("Veto duplicado: " + vr.idMatrizVeto());
            }
        }
    }

    Resultado calcularResultado(BigDecimal puntajeTotal, boolean tieneVeto) {
        if (tieneVeto) return Resultado.DESCARTAR;
        if (puntajeTotal.compareTo(UMBRAL_AVANZA) >= 0) return Resultado.AVANZA;
        if (puntajeTotal.compareTo(UMBRAL_REVISAR) >= 0) return Resultado.REVISAR;
        return Resultado.DESCARTAR;
    }

    Arquetipo calcularArquetipo(BigDecimal puntajeTotal, boolean tieneVeto, Integer scoreEsfuerzo) {
        if (tieneVeto) return Arquetipo.A_REVISAR;
        if (puntajeTotal.compareTo(UMBRAL_REVISAR) < 0) return Arquetipo.TIME_WASTER;
        if (puntajeTotal.compareTo(UMBRAL_AVANZA) >= 0) {
            if (scoreEsfuerzo == null) return Arquetipo.A_REVISAR;
            return scoreEsfuerzo >= UMBRAL_ESFUERZO_QUICK ? Arquetipo.QUICK_WIN : Arquetipo.MAJOR_PROJECT;
        }
        return Arquetipo.A_REVISAR;
    }

    Integer encontrarScoreEsfuerzo(Matriz matriz, List<EvaluacionScoreRequest> scores) {
        MatrizDimension dimEsfuerzo = matriz.getDimensiones().stream()
                .filter(d -> Boolean.TRUE.equals(d.getInvertida()))
                .findFirst()
                .orElse(null);

        if (dimEsfuerzo == null) {
            dimEsfuerzo = matriz.getDimensiones().stream()
                    .filter(d -> d.getNombre() != null && d.getNombre().toLowerCase().contains("esfuerzo"))
                    .findFirst()
                    .orElse(null);
        }
        if (dimEsfuerzo == null) return null;

        Long idEsfuerzo = dimEsfuerzo.getId();
        return scores.stream()
                .filter(s -> s.idMatrizDimension().equals(idEsfuerzo))
                .map(EvaluacionScoreRequest::score)
                .findFirst()
                .orElse(null);
    }

    EstadoIniciativa mapearEstado(Resultado resultado, boolean tieneVeto) {
        if (tieneVeto) return EstadoIniciativa.VETADO;
        return switch (resultado) {
            case AVANZA -> EstadoIniciativa.APROBADO;
            case REVISAR -> EstadoIniciativa.A_REVISAR;
            case DESCARTAR -> EstadoIniciativa.RECHAZADO;
        };
    }
}
