-- ==========================================
-- EXTENSIONES
-- ==========================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ==========================================
-- TABLA: roles
-- ==========================================
CREATE TABLE "roles" (
    "id" SERIAL PRIMARY KEY,
    "nombre" VARCHAR(50) NOT NULL UNIQUE,
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON COLUMN "roles"."nombre" IS 'Nombre único del rol. Ej: admin, seguridad, usuario.';
COMMENT ON COLUMN "roles"."enabled" IS 'Permite desactivar un rol sin eliminarlo.';


-- ==========================================
-- TABLA: usuarios
-- ==========================================
CREATE TABLE "usuarios" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "rol_id" INTEGER NOT NULL REFERENCES "roles"("id") ON DELETE RESTRICT,
    "nombre_completo" VARCHAR(150) NOT NULL,
    "correo" VARCHAR(150) NOT NULL UNIQUE,
    "documento" VARCHAR(20) NOT NULL UNIQUE,
    "tipo_usuario" VARCHAR(50) CHECK ("tipo_usuario" IN('alumno', 'docente', 'personal_admin')),
    "password_hash" VARCHAR(255) NOT NULL,
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "usuarios_rol_id_index" ON "usuarios"("rol_id");

COMMENT ON COLUMN "usuarios"."documento" IS 'DNI, pasaporte o código universitario. Único en el sistema.';
COMMENT ON COLUMN "usuarios"."tipo_usuario" IS 'Solo aplica cuando rol = usuario. Valores: alumno, docente, personal_admin.';
COMMENT ON COLUMN "usuarios"."enabled" IS 'FALSE = cuenta desactivada. No se elimina físicamente.';
COMMENT ON COLUMN "usuarios"."created_at" IS 'Fecha de creación del registro. Inmutable tras el INSERT.';


-- ==========================================
-- TABLA: vehiculos
-- ==========================================
CREATE TABLE "vehiculos" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "usuario_id" UUID NOT NULL REFERENCES "usuarios"("id") ON DELETE RESTRICT,
    "tipo" VARCHAR(15) NOT NULL CHECK ("tipo" IN('auto', 'moto')),
    "placa" VARCHAR(15) NOT NULL UNIQUE,
    "marca_modelo" VARCHAR(100) NOT NULL,
    "color" VARCHAR(30) NOT NULL,
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX "vehiculos_usuario_id_index" ON "vehiculos"("usuario_id");

COMMENT ON COLUMN "vehiculos"."placa" IS 'Identificador principal para control de acceso en garita. Única en el sistema.';
COMMENT ON COLUMN "vehiculos"."tipo" IS 'Tipo de vehículo. Valores: auto, moto.';


-- ==========================================
-- TABLA: campus
-- ==========================================
CREATE TABLE "campus" (
    "id" SERIAL PRIMARY KEY,
    "nombre" VARCHAR(100) NOT NULL UNIQUE,
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE
);


-- ==========================================
-- TABLA: zonas
-- ==========================================
CREATE TABLE "zonas" (
    "id" SERIAL PRIMARY KEY,
    "campus_id" INTEGER NOT NULL REFERENCES "campus"("id") ON DELETE RESTRICT,
    "nombre" VARCHAR(100) NOT NULL,
    "ubicacion" VARCHAR(150),
    "tipo" VARCHAR(30) NOT NULL,
    "aforo_maximo" INTEGER NOT NULL,
    "aforo_disponible" INTEGER NOT NULL,
    "estado" VARCHAR(20) NOT NULL DEFAULT 'activa' CHECK ("estado" IN('activa', 'cerrada')),
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE,
    "created_at" TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "zonas_campus_id_index" ON "zonas"("campus_id");

COMMENT ON COLUMN "zonas"."ubicacion" IS 'Descripción física o referencia de ubicación dentro del campus.';
COMMENT ON COLUMN "zonas"."aforo_maximo" IS 'Capacidad total configurada por el administrador.';
COMMENT ON COLUMN "zonas"."aforo_disponible" IS 'Espacios libres en tiempo real. Lo actualiza el trigger trg_actualizar_aforo.';
COMMENT ON COLUMN "zonas"."estado" IS 'activa = operativa. cerrada = sin acceso (mantenimiento o decisión admin).';
COMMENT ON COLUMN "zonas"."created_at" IS 'Fecha de creación del registro. Inmutable tras el INSERT.';


-- ==========================================
-- TABLA: accesos_vehiculares
-- ==========================================
CREATE TABLE "accesos_vehiculares" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "usuario_id" UUID NOT NULL REFERENCES "usuarios"("id") ON DELETE RESTRICT,
    "vehiculo_id" UUID NOT NULL REFERENCES "vehiculos"("id") ON DELETE RESTRICT,
    "zona_id" INTEGER NOT NULL REFERENCES "zonas"("id") ON DELETE RESTRICT,
    "guardia_entrada_id" UUID NOT NULL REFERENCES "usuarios"("id") ON DELETE RESTRICT,
    "guardia_salida_id" UUID REFERENCES "usuarios"("id") ON DELETE RESTRICT,
    "hora_ingreso" TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "hora_salida" TIMESTAMP(0) WITH TIME ZONE,
    "estado" VARCHAR(20) NOT NULL DEFAULT 'en_curso' CHECK ("estado" IN('en_curso', 'completada')),
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX "accesos_vehiculares_usuario_id_hora_ingreso_index" ON "accesos_vehiculares"("usuario_id", "hora_ingreso");
CREATE INDEX "accesos_vehiculares_usuario_id_index" ON "accesos_vehiculares"("usuario_id");
CREATE INDEX "accesos_vehiculares_vehiculo_id_index" ON "accesos_vehiculares"("vehiculo_id");
CREATE INDEX "accesos_vehiculares_zona_id_index" ON "accesos_vehiculares"("zona_id");
CREATE INDEX "accesos_vehiculares_estado_index" ON "accesos_vehiculares"("estado");

COMMENT ON COLUMN "accesos_vehiculares"."guardia_entrada_id" IS 'Guardia que registró el ingreso. Puede ser distinto al de salida.';
COMMENT ON COLUMN "accesos_vehiculares"."guardia_salida_id" IS 'Guardia que registró la salida. NULL mientras el vehículo sigue dentro.';
COMMENT ON COLUMN "accesos_vehiculares"."hora_salida" IS 'NULL = el vehículo está actualmente dentro del campus.';
COMMENT ON COLUMN "accesos_vehiculares"."estado" IS 'en_curso: vehículo dentro. completada: vehículo salió.';


-- ==========================================
-- TABLA: configuraciones
-- ==========================================
CREATE TABLE "configuraciones" (
    "id" SERIAL PRIMARY KEY,
    "clave" VARCHAR(100) NOT NULL UNIQUE,
    "valor" VARCHAR(255) NOT NULL,
    "descripcion" VARCHAR(255),
    "enabled" BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON COLUMN "configuraciones"."clave" IS 'Identificador único del parámetro. Convención: UPPER_SNAKE_CASE.';
COMMENT ON COLUMN "configuraciones"."valor" IS 'Valor actual. El sistema lo lee dinámicamente en cada operación.';


-- ==========================================
-- TABLA: logs_auditoria
-- ==========================================
CREATE TABLE "logs_auditoria" (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "usuario_id" UUID REFERENCES "usuarios"("id") ON DELETE RESTRICT,
    "tabla_afectada" VARCHAR(50) NOT NULL,
    "registro_id" VARCHAR(36) NOT NULL,
    "accion" VARCHAR(10) NOT NULL CHECK ("accion" IN('INSERT', 'UPDATE', 'DELETE')),
    "valores_anteriores" JSONB,
    "valores_nuevos" JSONB,
    "fecha" TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "logs_auditoria_tabla_afectada_fecha_index" ON "logs_auditoria"("tabla_afectada", "fecha");
CREATE INDEX "logs_auditoria_usuario_id_index" ON "logs_auditoria"("usuario_id");
CREATE INDEX "logs_auditoria_accion_index" ON "logs_auditoria"("accion");

COMMENT ON COLUMN "logs_auditoria"."usuario_id" IS 'NULL si el evento fue disparado automáticamente por un trigger.';
COMMENT ON COLUMN "logs_auditoria"."valores_anteriores" IS 'Estado de la fila ANTES del cambio. NULL en INSERT.';
COMMENT ON COLUMN "logs_auditoria"."valores_nuevos" IS 'Estado de la fila DESPUÉS del cambio. NULL en DELETE.';
