package com.correoargentino.evaluador.repository;

import com.correoargentino.evaluador.domain.Matriz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatrizRepository extends JpaRepository<Matriz, Long> {

    List<Matriz> findByActivaTrueOrderByFechaCreacionDesc();
}
