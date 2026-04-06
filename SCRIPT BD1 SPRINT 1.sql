-- =============================================
-- SPRINT 1 — Fundamentos y Acceso
-- =============================================

-- HU-01, HU-02: Registro e inicio de sesión
CREATE TABLE usuario (
    id                  BIGSERIAL PRIMARY KEY,
    correo_usuario      VARCHAR(150) UNIQUE NOT NULL,
    clave_usuario       VARCHAR(255) NOT NULL,
    nombres             VARCHAR(100),
    apellidos           VARCHAR(100),
    rol                 VARCHAR(30) DEFAULT 'AFICIONADO',
    estado              SMALLINT DEFAULT 1,
    intentos            INT DEFAULT 0,
    fecha_registro      TIMESTAMP DEFAULT NOW(),
    fecha_ultima_clave  TIMESTAMP
);

-- HU-03, HU-04: Preferencias del usuario
CREATE TABLE preferencia_usuario (
    id                      BIGSERIAL PRIMARY KEY,
    id_usuario              BIGINT NOT NULL REFERENCES usuario(id),
    selecciones_favoritas   VARCHAR(300),
    ciudades_interes        VARCHAR(300),
    notif_push              SMALLINT DEFAULT 1,
    notif_email             SMALLINT DEFAULT 1
);

-- HU-28: Logs estructurados con correlación
CREATE TABLE evento_auditoria (
    id                  BIGSERIAL PRIMARY KEY,
    id_correlacion      VARCHAR(100) NOT NULL,
    id_usuario          BIGINT REFERENCES usuario(id),
    tipo_evento         VARCHAR(60) NOT NULL,
    modulo_origen       VARCHAR(50),
    detalle             VARCHAR(1000),
    timestamp_evento    TIMESTAMP DEFAULT NOW(),
    estado_resultado    VARCHAR(20) DEFAULT 'OK'
);

-- Acciones en query tool 

-- Eliminar la restricción actual
ALTER TABLE preferencia_usuario 
DROP CONSTRAINT preferencia_usuario_id_usuario_fkey;

-- Volver a crearla con CASCADE
ALTER TABLE preferencia_usuario 
ADD CONSTRAINT preferencia_usuario_id_usuario_fkey 
FOREIGN KEY (id_usuario) 
REFERENCES usuario(id) 
ON DELETE CASCADE;

-- Hacer lo mismo para evento_auditoria
ALTER TABLE evento_auditoria 
DROP CONSTRAINT evento_auditoria_id_usuario_fkey;

ALTER TABLE evento_auditoria
ADD CONSTRAINT evento_auditoria_id_usuario_fkey
FOREIGN KEY (id_usuario)
REFERENCES usuario(id)
ON DELETE CASCADE;

-- HU-01b: Verificación de correo electrónico (SPRINT 1 - Actualización)
-- Nota: Con ddl-auto=update estas columnas se agregan automáticamente.
-- Ejecutar manualmente solo si la BD ya existía antes de este cambio.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS correo_verificado BOOLEAN DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS token_verificacion VARCHAR(200);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS fecha_expiracion_token TIMESTAMP;