package com.correoargentino.evaluador.repository;

import com.correoargentino.evaluador.domain.EstadoIniciativa;
import com.correoargentino.evaluador.domain.Iniciativa;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface IniciativaRepository extends JpaRepository<Iniciativa, Long>, JpaSpecificationExecutor<Iniciativa> {

    static Specification<Iniciativa> filtrar(EstadoIniciativa estado, String search) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (estado != null) {
                ps.add(cb.equal(root.get("estado"), estado));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), like),
                        cb.like(cb.lower(root.get("responsable")), like)
                ));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
