-- ==========================================
-- SEED: roles
-- ==========================================
-- Roles fijos del sistema. No se exponen vía CRUD; viven únicamente aquí.
INSERT INTO "roles" ("nombre", "enabled") VALUES
    ('ADMINISTRADOR',        TRUE),
    ('SEGURIDAD',            TRUE),
    ('COORDINADOR_SEGURIDAD', TRUE),
    ('USUARIO',              TRUE);


-- ==========================================
-- SEED: campus
-- ==========================================
-- Al menos un campus es obligatorio para poder crear zonas (zonas.campus_id NOT NULL).
INSERT INTO "campus" ("nombre", "enabled") VALUES
    ('Campus Central', TRUE);


-- ==========================================
-- SEED: configuraciones
-- ==========================================
-- Parámetros base leídos dinámicamente por la aplicación.
INSERT INTO "configuraciones" ("clave", "valor", "descripcion", "enabled") VALUES
    ('JWT_EXPIRATION_MINUTES', '60',  'Tiempo de expiración del token JWT en minutos.', TRUE),
    ('MAX_INTENTOS_LOGIN',     '5',   'Cantidad máxima de intentos de login fallidos permitidos.', TRUE);


-- ==========================================
-- SEED: usuario administrador inicial
-- ==========================================
-- Necesario para poder hacer login (HU-001) y acceder a los endpoints
-- restringidos a ADMINISTRADOR (HU-002, HU-010), ya que ningún endpoint
-- de la API puede crear el primer administrador.
--
-- Credenciales de desarrollo (NO usar en producción):
--   correo:    admin@unicampus.edu.pe
--   documento: 00000001
--   password:  Admin123!
--
-- El hash fue generado con BCrypt, strength 12:
--   $2b$12$pRRNZ64K92rD8GTOC.TGju8KiAiFNypxFnyNLJdULlxyg1is97mL6
INSERT INTO "usuarios" (
    "rol_id",
    "nombre_completo",
    "correo",
    "documento",
    "tipo_usuario",
    "password_hash",
    "enabled"
) VALUES (
    (SELECT "id" FROM "roles" WHERE "nombre" = 'ADMINISTRADOR'),
    'Administrador General',
    'admin@unicampus.edu.pe',
    '00000001',
    NULL,
    '$2b$12$pRRNZ64K92rD8GTOC.TGju8KiAiFNypxFnyNLJdULlxyg1is97mL6',
    TRUE
);
