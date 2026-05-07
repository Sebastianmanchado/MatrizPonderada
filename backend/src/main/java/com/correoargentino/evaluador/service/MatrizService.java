package com.correoargentino.evaluador.service;

import com.correoargentino.evaluador.domain.Matriz;
import com.correoargentino.evaluador.domain.MatrizDimension;
import com.correoargentino.evaluador.domain.MatrizVeto;
import com.correoargentino.evaluador.dto.MatrizDimensionRequest;
import com.correoargentino.evaluador.dto.MatrizRequest;
import com.correoargentino.evaluador.dto.MatrizResponse;
import com.correoargentino.evaluador.dto.MatrizVetoRequest;
import com.correoargentino.evaluador.exception.ConflictException;
import com.correoargentino.evaluador.exception.NotFoundException;
import com.correoargentino.evaluador.exception.ValidationException;
import com.correoargentino.evaluador.repository.EvaluacionRepository;
import com.correoargentino.evaluador.repository.MatrizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatrizService {

    static final BigDecimal TOLERANCIA = new BigDecimal("0.0001");
    static final BigDecimal PESO_TOTAL = BigDecimal.ONE;

    private final MatrizRepository matrizRepository;
    private final EvaluacionRepository evaluacionRepository;

    public MatrizService(MatrizRepository matrizRepository, EvaluacionRepository evaluacionRepository) {
        this.matrizRepository = matrizRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Transactional(readOnly = true)
    public List<MatrizResponse> listarActivas() {
        return matrizRepository.findByActivaTrueOrderByFechaCreacionDesc().stream()
                .map(m -> Mapper.toMatrizResponse(m, evaluacionRepository.existsByMatrizId(m.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public MatrizResponse obtener(Long id) {
        Matriz m = matrizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Matriz no encontrada: " + id));
        return Mapper.toMatrizResponse(m, evaluacionRepository.existsByMatrizId(m.getId()));
    }

    @Transactional
    public MatrizResponse crear(MatrizRequest req) {
        validarRequest(req);

        Matriz m = Matriz.builder()
                .nombre(req.nombre())
                .descripcion(req.descripcion())
                .activa(true)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador(req.usuarioCreador())
                .build();

        for (MatrizDimensionRequest d : req.dimensiones()) {
            m.addDimension(MatrizDimension.builder()
                    .nombre(d.nombre())
                    .descripcion(d.descripcion())
                    .peso(d.peso().setScale(4, RoundingMode.HALF_UP))
                    .orden(d.orden())
                    .invertida(d.invertida())
                    .build());
        }
        if (req.vetos() != null) {
            for (MatrizVetoRequest v : req.vetos()) {
                m.addVeto(MatrizVeto.builder()
                        .descripcion(v.descripcion())
                        .orden(v.orden())
                        .build());
            }
        }

        Matriz guardada = matrizRepository.save(m);
        return Mapper.toMatrizResponse(guardada, false);
    }

    @Transactional
    public MatrizResponse actualizar(Long id, MatrizRequest req) {
        Matriz existente = matrizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Matriz no encontrada: " + id));

        if (evaluacionRepository.existsByMatrizId(id)) {
            throw new ConflictException("Esta matriz ya fue usada en evaluaciones. Cloná y editá una nueva versión.");
        }

        validarRequest(req);

        existente.setNombre(req.nombre());
        existente.setDescripcion(req.descripcion());
        existente.setUsuarioCreador(req.usuarioCreador());

        existente.getDimensiones().clear();
        for (MatrizDimensionRequest d : req.dimensiones()) {
            existente.addDimension(MatrizDimension.builder()
                    .nombre(d.nombre())
                    .descripcion(d.descripcion())
                    .peso(d.peso().setScale(4, RoundingMode.HALF_UP))
                    .orden(d.orden())
                    .invertida(d.invertida())
                    .build());
        }

        existente.getVetos().clear();
        if (req.vetos() != null) {
            for (MatrizVetoRequest v : req.vetos()) {
                existente.addVeto(MatrizVeto.builder()
                        .descripcion(v.descripcion())
                        .orden(v.orden())
                        .build());
            }
        }

        return Mapper.toMatrizResponse(existente, false);
    }

    @Transactional
    public void desactivar(Long id) {
        Matriz m = matrizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Matriz no encontrada: " + id));
        m.setActiva(false);
    }

    void validarRequest(MatrizRequest req) {
        if (req.dimensiones() == null || req.dimensiones().isEmpty()) {
            throw new ValidationException("La matriz debe tener al menos una dimensión.");
        }

        BigDecimal suma = req.dimensiones().stream()
                .map(MatrizDimensionRequest::peso)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (suma.subtract(PESO_TOTAL).abs().compareTo(TOLERANCIA) > 0) {
            throw new ValidationException(
                    "La suma de pesos de las dimensiones debe ser 1.0 (actual: " + suma + ")."
            );
        }

        Set<Integer> ordenesDim = new HashSet<>();
        for (MatrizDimensionRequest d : req.dimensiones()) {
            if (!ordenesDim.add(d.orden())) {
                throw new ValidationException("El orden de las dimensiones debe ser único. Duplicado: " + d.orden());
            }
        }

        if (req.vetos() != null) {
            Set<Integer> ordenesVeto = new HashSet<>();
            for (MatrizVetoRequest v : req.vetos()) {
                if (!ordenesVeto.add(v.orden())) {
                    throw new ValidationException("El orden de los vetos debe ser único. Duplicado: " + v.orden());
                }
            }
        }
    }
}
