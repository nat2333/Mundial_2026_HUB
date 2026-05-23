-- ============================================================
-- MÓDULO SEDES — FIFA WORLD CUP 2026
-- Sedes oficiales confirmadas: USA (11), México (3), Canadá (2)
-- Total: 16 estadios
-- ============================================================

CREATE TABLE IF NOT EXISTS sede (
    id          BIGSERIAL PRIMARY KEY,
    nombre_estadio  VARCHAR(255)    NOT NULL,
    ciudad          VARCHAR(100)    NOT NULL,
    pais            VARCHAR(100)    NOT NULL,
    capacidad       INTEGER,
    latitud         DOUBLE PRECISION,
    longitud        DOUBLE PRECISION,
    imagen_url      VARCHAR(2000), -- URLs separadas por coma: 'url1,url2,url3'
    descripcion     VARCHAR(1000)
);

-- Limpiar datos previos si se vuelve a ejecutar
TRUNCATE TABLE sede RESTART IDENTITY CASCADE;

-- ============================================================
-- ESTADOS UNIDOS — 11 sedes
-- ============================================================

INSERT INTO sede (nombre_estadio, ciudad, pais, capacidad, latitud, longitud, imagen_url, descripcion) VALUES
(
    'MetLife Stadium',
    'East Rutherford',
    'USA',
    82500,
    40.8135,
    -74.0745,
    'https://stadiumdb.com/pictures/stadiums/usa/meadowlands_stadium/meadowlands_stadium18.jpg,https://stadiumdb.com/pictures/stadiums/usa/meadowlands_stadium/meadowlands_stadium09.jpg',
    'Estadio más grande de la NFL, hogar de los New York Giants y Jets. Sede de la FINAL del Mundial 2026. Capacidad para 82.500 espectadores.'
),
(
    'AT&T Stadium',
    'Arlington',
    'USA',
    80000,
    32.7480,
    -97.0929,
    'https://stadiumdb.com/pictures/stadiums/usa/cowboys_stadium/cowboys_stadium55.jpg,https://stadiumdb.com/pictures/stadiums/usa/cowboys_stadium/cowboys_stadium37.jpg',
    'Hogar de los Dallas Cowboys. Moderno estadio con techo retráctil y la pantalla de marcador más grande del mundo.'
),
(
    'SoFi Stadium',
    'Inglewood',
    'USA',
    70240,
    33.9535,
    -118.3390,
    'https://stadiumdb.com/pictures/stadiums/usa/sofi_stadium/sofi_stadium273.jpg,https://stadiumdb.com/pictures/stadiums/usa/sofi_stadium/sofi_stadium263.jpg',
    'Estadio de última generación en Los Ángeles, hogar de los Rams y Chargers. Inaugurado en 2020.'
),
(
    'Levi''s Stadium',
    'Santa Clara',
    'USA',
    68500,
    37.4033,
    -121.9694,
    'https://stadiumdb.com/pictures/stadiums/usa/levis_stadium/levis_stadium34.jpg,https://stadiumdb.com/pictures/stadiums/usa/levis_stadium/levis_stadium17.jpg',
    'Sede de los San Francisco 49ers, ubicado en el corazón de Silicon Valley. Inaugurado en 2014.'
),
(
    'Lincoln Financial Field',
    'Filadelfia',
    'USA',
    69796,
    39.9008,
    -75.1675,
    'https://stadiumdb.com/pictures/stadiums/usa/lincoln_financial_field/lincoln_financial_field20.jpg,https://stadiumdb.com/pictures/stadiums/usa/lincoln_financial_field/lincoln_financial_field18.jpg',
    'Hogar de los Philadelphia Eagles. Ciudad con gran tradición futbolística en la Costa Este.'
),
(
    'Arrowhead Stadium',
    'Kansas City',
    'USA',
    76416,
    39.0489,
    -94.4839,
    'https://stadiumdb.com/pictures/stadiums/usa/arrowhead_stadium/arrowhead_stadium10.jpg,https://stadiumdb.com/pictures/stadiums/usa/arrowhead_stadium/arrowhead_stadium31.jpg',
    'Hogar de los Kansas City Chiefs, considerado uno de los estadios más ruidosos de la NFL.'
),
(
    'NRG Stadium',
    'Houston',
    'USA',
    72220,
    29.6847,
    -95.4107,
    'https://stadiumdb.com/pictures/stadiums/usa/reliant_stadium/reliant_stadium30.jpg,https://stadiumdb.com/pictures/stadiums/usa/reliant_stadium/reliant_stadium27.jpg',
    'Primer estadio con techo retráctil de la NFL. Hogar de los Houston Texans. Inaugurado en 2002.'
),
(
    'Lumen Field',
    'Seattle',
    'USA',
    72000,
    47.5952,
    -122.3316,
    'https://stadiumdb.com/pictures/stadiums/usa/seahawks_stadium/seahawks_stadium02.jpg,https://stadiumdb.com/pictures/stadiums/usa/seahawks_stadium/seahawks_stadium10.jpg',
    'Hogar de los Seattle Seahawks y Seattle Sounders FC. Conocido por su ambiente electrizante.'
),
(
    'Hard Rock Stadium',
    'Miami Gardens',
    'USA',
    64767,
    25.9580,
    -80.2389,
    'https://stadiumdb.com/pictures/stadiums/usa/dolphins_stadium/dolphins_stadium41.jpg,https://stadiumdb.com/pictures/stadiums/usa/dolphins_stadium/dolphins_stadium12.jpg',
    'Sede de los Miami Dolphins, ubicado al norte de Miami. Fue renovado en 2016.'
),
(
    'Mercedes-Benz Stadium',
    'Atlanta',
    'USA',
    71000,
    33.7554,
    -84.4009,
    'https://stadiumdb.com/pictures/stadiums/usa/mercedes_benz_stadium/mercedes_benz_stadium32.jpg,https://stadiumdb.com/pictures/stadiums/usa/mercedes_benz_stadium/mercedes_benz_stadium58.jpg',
    'Estadio multiusos con techo retráctil en forma de flor. Hogar de los Atlanta Falcons y Atlanta United FC.'
),
(
    'Gillette Stadium',
    'Foxborough',
    'USA',
    65878,
    42.0909,
    -71.2643,
    'https://stadiumdb.com/pictures/stadiums/usa/gillette_stadium/gillette_stadium11.jpg,https://stadiumdb.com/pictures/stadiums/usa/gillette_stadium/gillette_stadium26.jpg',
    'Hogar de los New England Patriots y New England Revolution. Ubicado a 45 km al sur de Boston.'
);

-- ============================================================
-- MÉXICO — 3 sedes
-- ============================================================

INSERT INTO sede (nombre_estadio, ciudad, pais, capacidad, latitud, longitud, imagen_url, descripcion) VALUES
(
    'Estadio Azteca',
    'Ciudad de México',
    'México',
    87523,
    19.3029,
    -99.1506,
    'https://stadiumdb.com/pictures/stadiums/mex/estadio_azteca/estadio_azteca76.jpg,https://stadiumdb.com/pictures/stadiums/mex/estadio_azteca/estadio_azteca20.jpg',
    'El Coloso de Santa Úrsula. Único estadio del mundo en albergar dos finales de Copa del Mundo (1970 y 1986). El más grande de América Latina.'
),
(
    'Estadio BBVA',
    'Guadalupe',
    'México',
    53500,
    25.6694,
    -100.2408,
    'https://stadiumdb.com/pictures/stadiums/mex/estadio_bbva_bancomer/estadio_bbva_bancomer90.jpg,https://stadiumdb.com/pictures/stadiums/mex/estadio_bbva_bancomer/estadio_bbva_bancomer73.jpg',
    'Estadio de los Rayados de Monterrey. Inaugurado en 2015, destaca por el espectacular telón de fondo del Cerro de la Silla.'
),
(
    'Estadio Akron',
    'Zapopan',
    'México',
    49850,
    20.6464,
    -103.4679,
    'https://stadiumdb.com/pictures/stadiums/mex/estadio_omnilife/estadio_omnilife66.jpg,https://stadiumdb.com/pictures/stadiums/mex/estadio_omnilife/estadio_omnilife11.jpg',
    'Sede del Club Deportivo Guadalajara (Chivas). Conocido como la Perla Tapatía, inaugurado en 2010 en la zona metropolitana de Guadalajara.'
);

-- ============================================================
-- CANADÁ — 2 sedes
-- ============================================================

INSERT INTO sede (nombre_estadio, ciudad, pais, capacidad, latitud, longitud, imagen_url, descripcion) VALUES
(
    'BC Place',
    'Vancouver',
    'Canadá',
    54500,
    49.2767,
    -123.1122,
    'https://stadiumdb.com/pictures/stadiums/can/bc_place/bc_place26.jpg,https://stadiumdb.com/pictures/stadiums/can/bc_place/bc_place47.jpg',
    'Estadio techado más grande de Canadá, hogar del Vancouver Whitecaps. Renovado en 2011 con un nuevo techo de membrana retráctil.'
),
(
    'BMO Field',
    'Toronto',
    'Canadá',
    45000,
    43.6333,
    -79.4187,
    'https://static1.gensler.com/uploads/hero_element/9544/thumb_desktop/thumbs/project_BMO_1024x576_01_1475515202_1024x576.jpg,https://stadiumdb.com/pictures/stadiums/can/bmo_field/bmo_field42.jpg',
    'Hogar del Toronto FC, primer estadio de fútbol de formato europeo construido en Canadá. Ampliado en 2016.'
);

-- ============================================================
-- Verificación
-- ============================================================
-- SELECT pais, COUNT(*) as total FROM sede GROUP BY pais ORDER BY total DESC;
-- SELECT * FROM sede ORDER BY pais, ciudad;
