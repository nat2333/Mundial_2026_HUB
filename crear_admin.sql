-- Usuario administrador del sistema
-- Contraseña: Admin2026!  (SHA-1)
-- Ejecutar UNA SOLA VEZ contra la BD de producción/desarrollo

INSERT INTO usuario (
    correo_usuario,
    clave_usuario,
    nombres,
    apellidos,
    rol,
    estado,
    intentos,
    fecha_registro,
    correo_verificado,
    token_verificacion,
    fecha_expiracion_token,
    fecha_ultima_clave
) VALUES (
    'admin@mundial2026.com',
    '957cc21fe69b5d057afac6e6f5a39bd5c3e31e25',
    'admin',
    'sistema',
    'ADMIN',
    1,
    0,
    NOW(),
    true,
    NULL,
    NULL,
    NOW()
);
