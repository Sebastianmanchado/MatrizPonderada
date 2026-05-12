-- =====================================================================
-- V2: versionado de iniciativas
--   * Nueva tabla iniciativa_version (snapshot inmutable del contenido)
--   * iniciativa.numero_version_actual: puntero a la versión vigente
--   * evaluacion.id_iniciativa_version: cada evaluación apunta a la versión
--     concreta que se evaluó (snapshot histórico)
--   * Backfill: por cada iniciativa existente se crea v1 con su contenido
--     actual y todas las evaluaciones existentes pasan a apuntar a esa v1.
-- =====================================================================

CREATE TABLE iniciativa_version (
    id                    BIGINT          IDENTITY(1,1) PRIMARY KEY,
    id_iniciativa         BIGINT          NOT NULL,
    numero_version        INT             NOT NULL,
    titulo                NVARCHAR(200)   NOT NULL,
    descripcion_problema  NVARCHAR(2000)  NOT NULL,
    descripcion_solucion  NVARCHAR(2000)  NOT NULL,
    area_solicitante      NVARCHAR(150)   NOT NULL,
    responsable           NVARCHAR(150)   NOT NULL,
    sponsor_ejecutivo     NVARCHAR(150)   NOT NULL,
    impacto_esperado      NVARCHAR(2000)  NOT NULL,
    datos_disponibles     NVARCHAR(2000)  NOT NULL,
    usuario_version       NVARCHAR(150)   NOT NULL,
    fecha_version         DATETIMEOFFSET  NOT NULL,
    comentario_version    NVARCHAR(1000)  NULL,
    CONSTRAINT fk_iniver_iniciativa FOREIGN KEY (id_iniciativa) REFERENCES iniciativa(id) ON DELETE CASCADE,
    CONSTRAINT uq_iniver_iniciativa_numero UNIQUE (id_iniciativa, numero_version)
);
CREATE INDEX ix_iniver_iniciativa ON iniciativa_version(id_iniciativa);

-- Puntero a la versión actual de la iniciativa.
ALTER TABLE iniciativa
    ADD numero_version_actual INT NOT NULL CONSTRAINT df_iniciativa_numero_version DEFAULT 1;

-- =====================================================================
-- Backfill: crear v1 para todas las iniciativas existentes
-- =====================================================================
INSERT INTO iniciativa_version (
    id_iniciativa, numero_version,
    titulo, descripcion_problema, descripcion_solucion,
    area_solicitante, responsable, sponsor_ejecutivo,
    impacto_esperado, datos_disponibles,
    usuario_version, fecha_version, comentario_version
)
SELECT
    i.id, 1,
    i.titulo, i.descripcion_problema, i.descripcion_solucion,
    i.area_solicitante, i.responsable, i.sponsor_ejecutivo,
    i.impacto_esperado, i.datos_disponibles,
    i.usuario_creador, i.fecha_creacion, N'Versión inicial'
FROM iniciativa i;

-- =====================================================================
-- Linkeo de evaluación a versión de iniciativa
-- =====================================================================
ALTER TABLE evaluacion ADD id_iniciativa_version BIGINT NULL;

-- Backfill: las evaluaciones existentes apuntan a la v1 de su iniciativa.
UPDATE e
   SET e.id_iniciativa_version = iv.id
  FROM evaluacion e
  JOIN iniciativa_version iv
    ON iv.id_iniciativa = e.id_iniciativa
   AND iv.numero_version = 1;

-- Una vez backfilleado, marco como NOT NULL y agrego la FK.
ALTER TABLE evaluacion ALTER COLUMN id_iniciativa_version BIGINT NOT NULL;

ALTER TABLE evaluacion
    ADD CONSTRAINT fk_eval_iniver FOREIGN KEY (id_iniciativa_version)
        REFERENCES iniciativa_version(id);

CREATE INDEX ix_eval_iniver ON evaluacion(id_iniciativa_version);
