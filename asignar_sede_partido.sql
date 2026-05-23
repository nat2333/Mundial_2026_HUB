-- ============================================================
-- Asignar sede_id a cada partido por id_externo
-- Reemplaza sedes-partido.txt — compatible con el nuevo esquema FK
-- Ejecutar DESPUÉS de sedes_mundial2026.sql y de tener partidos en BD
-- ============================================================

-- Fase de Grupos
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Azteca')         WHERE id_externo = '537327';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Akron')          WHERE id_externo = '537328';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537333';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537345';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537334';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537339';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537340';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537346';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537351';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537357';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537352';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio BBVA')           WHERE id_externo = '537358';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537369';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537363';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537370';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537364';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537391';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537392';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537397';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537398';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537403';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537409';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537410';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Azteca')         WHERE id_externo = '537404';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537329';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537335';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537336';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Akron')          WHERE id_externo = '537330';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537348';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537342';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537341';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537347';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537359';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537353';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537354';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio BBVA')           WHERE id_externo = '537360';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537371';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537365';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537372';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537366';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537399';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537393';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537394';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537400';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537405';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537411';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537412';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Akron')          WHERE id_externo = '537406';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537337';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537338';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537344';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537343';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Azteca')         WHERE id_externo = '537331';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio BBVA')           WHERE id_externo = '537332';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537355';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537356';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537361';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537362';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537349';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537350';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537395';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537396';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537373';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Akron')          WHERE id_externo = '537374';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537367';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537368';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537413';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537414';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537407';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537408';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537401';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537402';

-- Dieciseisavos de final (LAST_32)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537417';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537423';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio BBVA')           WHERE id_externo = '537415';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537418';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537424';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537416';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Azteca')         WHERE id_externo = '537425';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537426';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Levi''s Stadium')        WHERE id_externo = '537422';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537421';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BMO Field')              WHERE id_externo = '537420';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537419';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537429';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537428';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537427';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537430';

-- Octavos de final (LAST_16)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lincoln Financial Field') WHERE id_externo = '537376';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'NRG Stadium')            WHERE id_externo = '537375';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537377';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Estadio Azteca')         WHERE id_externo = '537378';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537379';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Lumen Field')            WHERE id_externo = '537380';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537381';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'BC Place')               WHERE id_externo = '537382';

-- Cuartos de final (QUARTER_FINALS)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Gillette Stadium')       WHERE id_externo = '537383';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'SoFi Stadium')           WHERE id_externo = '537384';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537385';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Arrowhead Stadium')      WHERE id_externo = '537386';

-- Semifinales (SEMI_FINALS)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'AT&T Stadium')           WHERE id_externo = '537387';
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Mercedes-Benz Stadium')  WHERE id_externo = '537388';

-- Tercer puesto (THIRD_PLACE)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'Hard Rock Stadium')      WHERE id_externo = '537389';

-- Final (FINAL)
UPDATE partido SET sede_id = (SELECT id FROM sede WHERE nombre_estadio = 'MetLife Stadium')        WHERE id_externo = '537390';

-- Verificación: partidos sin sede asignada (debe devolver 0 filas)
-- SELECT id, equipo_local, equipo_visitante FROM partido WHERE sede_id IS NULL;
