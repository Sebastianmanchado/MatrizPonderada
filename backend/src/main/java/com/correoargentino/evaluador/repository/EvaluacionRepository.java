package com.correoargentino.evaluador.repository;

import com.correoargentino.evaluador.domain.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Long> {

    List<Evaluacion> findByIniciativaIdOrderByFechaEvaluacionDesc(Long idIniciativa);

    boolean existsByMatrizId(Long idMatriz);
}
