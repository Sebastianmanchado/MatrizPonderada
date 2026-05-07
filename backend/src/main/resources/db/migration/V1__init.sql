-- =====================================================================
-- V1: esquema inicial + seed de la matriz "Evaluador Correo Argentino"
-- =====================================================================

CREATE TABLE iniciativa (
    id                    BIGINT          IDENTITY(1,1) PRIMARY KEY,
    titulo                NVARCHAR(200)   NOT NULL,
    descripcion_problema  NVARCHAR(2000)  NOT NULL,
    descripcion_solucion  NVARCHAR(2000)  NOT NULL,
    area_solicitante      NVARCHAR(150)   NOT NULL,
    responsable           NVARCHAR(150)   NOT NULL,
    sponsor_ejecutivo     NVARCHAR(150)   NOT NULL,
    impacto_esperado      NVARCHAR(2000)  NOT NULL,
    datos_disponibles     NVARCHAR(2000)  NOT NULL,
    estado                VARCHAR(30)     NOT NULL,
    fecha_creacion        DATETIMEOFFSET  NOT NULL,
    usuario_creador       NVARCHAR(150)   NOT NULL,
    CONSTRAINT ck_iniciativa_estado CHECK (estado IN ('SIN_EVALUAR','RECHAZADO','A_REVISAR','APROBADO','VETADO'))
);
CREATE INDEX ix_iniciativa_estado ON iniciativa(estado);
CREATE INDEX ix_iniciativa_titulo ON iniciativa(titulo);

CREATE TABLE matriz (
    id               BIGINT          IDENTITY(1,1) PRIMARY KEY,
    nombre           NVARCHAR(200)   NOT NULL,
    descripcion      NVARCHAR(1000)  NULL,
    activa           BIT             NOT NULL,
    fecha_creacion   DATETIMEOFFSET  NOT NULL,
    usuario_creador  NVARCHAR(150)   NOT NULL
);

CREATE TABLE matriz_dimension (
    id           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    id_matriz    BIGINT          NOT NULL,
    nombre       NVARCHAR(200)   NOT NULL,
    descripcion  NVARCHAR(1000)  NULL,
    peso         DECIMAL(8,4)    NOT NULL,
    orden        INT             NOT NULL,
    invertida    BIT             NOT NULL,
    CONSTRAINT fk_matriz_dim_matriz FOREIGN KEY (id_matriz) REFERENCES matriz(id) ON DELETE CASCADE,
    CONSTRAINT uq_matriz_dim_orden UNIQUE (id_matriz, orden)
);

CREATE TABLE matriz_veto (
    id           BIGINT          IDENTITY(1,1) PRIMARY KEY,
    id_matriz    BIGINT          NOT NULL,
    descripcion  NVARCHAR(1000)  NOT NULL,
    orden        INT             NOT NULL,
    CONSTRAINT fk_matriz_veto_matriz FOREIGN KEY (id_matriz) REFERENCES matriz(id) ON DELETE CASCADE,
    CONSTRAINT uq_matriz_veto_orden UNIQUE (id_matriz, orden)
);

CREATE TABLE evaluacion (
    id                BIGINT          IDENTITY(1,1) PRIMARY KEY,
    id_iniciativa     BIGINT          NOT NULL,
    id_matriz         BIGINT          NOT NULL,
    usuario_evaluador NVARCHAR(150)   NOT NULL,
    fecha_evaluacion  DATETIMEOFFSET  NOT NULL,
    puntaje_total     DECIMAL(6,2)    NOT NULL,
    arquetipo         VARCHAR(30)     NOT NULL,
    resultado         VARCHAR(30)     NOT NULL,
    tiene_veto        BIT             NOT NULL,
    notas             NVARCHAR(2000)  NULL,
    CONSTRAINT fk_eval_iniciativa FOREIGN KEY (id_iniciativa) REFERENCES iniciativa(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_matriz     FOREIGN KEY (id_matriz)     REFERENCES matriz(id),
    CONSTRAINT ck_eval_arquetipo  CHECK (arquetipo IN ('QUICK_WIN','MAJOR_PROJECT','TIME_WASTER','A_REVISAR')),
    CONSTRAINT ck_eval_resultado  CHECK (resultado IN ('AVANZA','REVISAR','DESCARTAR'))
);
CREATE INDEX ix_eval_iniciativa ON evaluacion(id_iniciativa);
CREATE INDEX ix_eval_fecha      ON evaluacion(fecha_evaluacion DESC);

CREATE TABLE evaluacion_score (
    id                  BIGINT       IDENTITY(1,1) PRIMARY KEY,
    id_evaluacion       BIGINT       NOT NULL,
    id_matriz_dimension BIGINT       NOT NULL,
    score               INT          NOT NULL,
    puntaje_ponderado   DECIMAL(8,4) NOT NULL,
    CONSTRAINT fk_evscore_eval FOREIGN KEY (id_evaluacion)       REFERENCES evaluacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_evscore_dim  FOREIGN KEY (id_matriz_dimension) REFERENCES matriz_dimension(id),
    CONSTRAINT uq_evaluacion_dim UNIQUE (id_evaluacion, id_matriz_dimension),
    CONSTRAINT ck_evscore_rango CHECK (score BETWEEN 1 AND 5)
);

CREATE TABLE evaluacion_veto (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_evaluacion   BIGINT NOT NULL,
    id_matriz_veto  BIGINT NOT NULL,
    aplica          BIT    NOT NULL,
    CONSTRAINT fk_evveto_eval        FOREIGN KEY (id_evaluacion)  REFERENCES evaluacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_evveto_matrizveto  FOREIGN KEY (id_matriz_veto) REFERENCES matriz_veto(id),
    CONSTRAINT uq_evaluacion_veto UNIQUE (id_evaluacion, id_matriz_veto)
);

-- =====================================================================
-- Seed: matriz "Evaluador Correo Argentino — 6 dimensiones"
-- =====================================================================

DECLARE @ahora DATETIMEOFFSET = SYSDATETIMEOFFSET();
DECLARE @id_matriz BIGINT;

INSERT INTO matriz (nombre, descripcion, activa, fecha_creacion, usuario_creador)
VALUES (
    N'Evaluador Correo Argentino - 6 dimensiones',
    N'Matriz inicial con seis dimensiones ponderadas para evaluar iniciativas de IA en Correo Argentino.',
    1,
    @ahora,
    N'system'
);

SET @id_matriz = SCOPE_IDENTITY();

INSERT INTO matriz_dimension (id_matriz, nombre, descripcion, peso, orden, invertida) VALUES
    (@id_matriz, N'Impacto de negocio',         N'Magnitud del beneficio esperado en KPIs operativos, financieros o de experiencia.', 0.3000, 1, 0),
    (@id_matriz, N'Viabilidad de datos',        N'Disponibilidad, calidad y acceso a los datos necesarios para entrenar y operar la solución.', 0.2500, 2, 0),
    (@id_matriz, N'Esfuerzo e integración',     N'Complejidad técnica e integración con sistemas existentes (5 = mínima complejidad).', 0.1500, 3, 1),
    (@id_matriz, N'Tiempo al primer valor',     N'Velocidad para entregar el primer beneficio tangible al negocio.', 0.1500, 4, 0),
    (@id_matriz, N'Riesgo',                     N'Riesgos regulatorios, éticos, reputacionales u operativos (5 = menor riesgo).', 0.1000, 5, 0),
    (@id_matriz, N'Alineación estratégica',     N'Grado de alineación con los objetivos estratégicos de la organización.', 0.0500, 6, 0);

INSERT INTO matriz_veto (id_matriz, descripcion, orden) VALUES
    (@id_matriz, N'Los datos o la información necesaria no existen ni están en el roadmap de los próximos 6 meses', 1),
    (@id_matriz, N'No hay sponsor ejecutivo identificado con nombre y apellido para esta iniciativa',               2),
    (@id_matriz, N'Depende de infraestructura que no estará disponible en los próximos 12 meses',                   3);
