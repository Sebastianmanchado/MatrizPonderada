package com.correoargentino.evaluador.repository;

import com.correoargentino.evaluador.domain.IniciativaVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IniciativaVersionRepository extends JpaRepository<IniciativaVersion, Long> {

    List<IniciativaVersion> findByIniciativaIdOrderByNumeroVersionDesc(Long idIniciativa);

    Optional<IniciativaVersion> findByIniciativaIdAndNumeroVersion(Long idIniciativa, Integer numeroVersion);
}
