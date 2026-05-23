-- ============================================================
-- SEED: Entradas de prueba por partido
-- Genera 12 entradas por partido (3 tribunas x 4 entradas)
-- Solo inserta si el partido existe y no hay entradas previas
-- Ejecutar en PostgreSQL contra la BD del proyecto
-- ============================================================

DO $$
DECLARE
    rec        RECORD;
    precio_gen NUMERIC := 120000;
    precio_pla NUMERIC := 250000;
    precio_vip NUMERIC := 500000;
    i          INT;
BEGIN
    FOR rec IN
        SELECT id FROM partido WHERE estado = 'PROGRAMADO'
    LOOP
        -- Solo inserta si ese partido aún no tiene entradas
        IF NOT EXISTS (SELECT 1 FROM entrada WHERE id_partido = rec.id) THEN

            -- 4 entradas tribuna GENERAL
            FOR i IN 1..4 LOOP
                INSERT INTO entrada (id_partido, estado, precio, tribuna)
                VALUES (rec.id, 'DISPONIBLE', precio_gen, 'GENERAL');
            END LOOP;

            -- 4 entradas tribuna PLATEA
            FOR i IN 1..4 LOOP
                INSERT INTO entrada (id_partido, estado, precio, tribuna)
                VALUES (rec.id, 'DISPONIBLE', precio_pla, 'PLATEA');
            END LOOP;

            -- 4 entradas tribuna VIP
            FOR i IN 1..4 LOOP
                INSERT INTO entrada (id_partido, estado, precio, tribuna)
                VALUES (rec.id, 'DISPONIBLE', precio_vip, 'VIP');
            END LOOP;

            RAISE NOTICE 'Creadas 12 entradas para partido id=%', rec.id;
        ELSE
            RAISE NOTICE 'Partido id=% ya tiene entradas, omitido.', rec.id;
        END IF;
    END LOOP;
END $$;

-- Verificar resultado
SELECT
    p.equipo_local || ' vs ' || p.equipo_visitante AS partido,
    e.tribuna,
    e.estado,
    COUNT(*)                                        AS cantidad,
    e.precio
FROM entrada e
JOIN partido p ON p.id = e.id_partido
GROUP BY p.equipo_local, p.equipo_visitante, e.tribuna, e.estado, e.precio
ORDER BY partido, e.tribuna;
