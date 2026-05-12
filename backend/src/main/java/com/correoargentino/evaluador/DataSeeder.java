package com.correoargentino.evaluador;

import com.correoargentino.evaluador.domain.Matriz;
import com.correoargentino.evaluador.domain.MatrizDimension;
import com.correoargentino.evaluador.domain.MatrizVeto;
import com.correoargentino.evaluador.repository.MatrizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Carga inicial de datos. Reemplaza al INSERT seed que estaba en la
 * migración Flyway V1, ya que con {@code ddl-auto=update} Flyway queda
 * desactivado y Hibernate gestiona el schema directamente.
 *
 * Es idempotente: si ya existe alguna matriz, no hace nada.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MatrizRepository matrizRepository;

    public DataSeeder(MatrizRepository matrizRepository) {
        this.matrizRepository = matrizRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (matrizRepository.count() > 0) {
            log.info("DataSeeder: ya hay matrices cargadas, salto el seed.");
            return;
        }
        log.info("DataSeeder: cargando matriz inicial 'Evaluador Correo Argentino - 6 dimensiones'.");

        Matriz m = Matriz.builder()
                .nombre("Evaluador Correo Argentino - 6 dimensiones")
                .descripcion("Matriz inicial con seis dimensiones ponderadas para evaluar iniciativas de IA en Correo Argentino.")
                .activa(true)
                .fechaCreacion(OffsetDateTime.now())
                .usuarioCreador("system")
                .build();

        m.addDimension(dim("Impacto de negocio",      "Magnitud del beneficio esperado en KPIs operativos, financieros o de experiencia.", "0.3000", 1, false));
        m.addDimension(dim("Viabilidad de datos",     "Disponibilidad, calidad y acceso a los datos necesarios para entrenar y operar la solución.", "0.2500", 2, false));
        m.addDimension(dim("Esfuerzo e integración",  "Complejidad técnica e integración con sistemas existentes (5 = mínima complejidad).", "0.1500", 3, true));
        m.addDimension(dim("Tiempo al primer valor",  "Velocidad para entregar el primer beneficio tangible al negocio.", "0.1500", 4, false));
        m.addDimension(dim("Riesgo",                  "Riesgos regulatorios, éticos, reputacionales u operativos (5 = menor riesgo).", "0.1000", 5, false));
        m.addDimension(dim("Alineación estratégica",  "Grado de alineación con los objetivos estratégicos de la organización.", "0.0500", 6, false));

        m.addVeto(veto("Los datos o la información necesaria no existen ni están en el roadmap de los próximos 6 meses", 1));
        m.addVeto(veto("No hay sponsor ejecutivo identificado con nombre y apellido para esta iniciativa", 2));
        m.addVeto(veto("Depende de infraestructura que no estará disponible en los próximos 12 meses", 3));

        matrizRepository.save(m);
        log.info("DataSeeder: matriz inicial creada con id={}", m.getId());
    }

    private static MatrizDimension dim(String nombre, String descripcion, String peso, int orden, boolean invertida) {
        return MatrizDimension.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .peso(new BigDecimal(peso))
                .orden(orden)
                .invertida(invertida)
                .build();
    }

    private static MatrizVeto veto(String descripcion, int orden) {
        return MatrizVeto.builder()
                .descripcion(descripcion)
                .orden(orden)
                .build();
    }
}
