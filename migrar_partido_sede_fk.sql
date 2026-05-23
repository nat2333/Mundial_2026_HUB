-- ============================================================
-- MIGRACIÓN: Vincular partido con sede mediante FK
-- Ejecutar DESPUÉS de que existan datos en la tabla sede
-- ============================================================

-- 1. Agregar columna FK (nullable para no romper filas existentes)
ALTER TABLE partido ADD COLUMN IF NOT EXISTS sede_id BIGINT;

-- 2. Poblar sede_id haciendo match por nombre_estadio
UPDATE partido p
SET sede_id = s.id
FROM sede s
WHERE p.estadio = s.nombre_estadio;

-- 3. Agregar la restricción de FK
ALTER TABLE partido
    ADD CONSTRAINT fk_partido_sede
    FOREIGN KEY (sede_id) REFERENCES sede(id);

-- 4. Eliminar columnas redundantes
ALTER TABLE partido DROP COLUMN IF EXISTS estadio;
ALTER TABLE partido DROP COLUMN IF EXISTS ciudad;

-- Verificación: partidos sin sede asignada (debería ser 0)
-- SELECT id, equipo_local, equipo_visitante FROM partido WHERE sede_id IS NULL;
