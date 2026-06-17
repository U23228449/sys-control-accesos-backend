-- ==========================================
-- FUNCION: fn_registrar_auditoria()
-- ==========================================
CREATE OR REPLACE FUNCTION fn_registrar_auditoria()
RETURNS TRIGGER AS $$
DECLARE
    v_old_json JSONB := NULL;
    v_new_json JSONB := NULL;
    v_reg_id VARCHAR(36);
BEGIN
    IF (TG_OP = 'INSERT') THEN
        v_new_json := row_to_json(NEW)::jsonb;
        v_reg_id := NEW.id::text;
    ELSIF (TG_OP = 'UPDATE') THEN
        v_old_json := row_to_json(OLD)::jsonb;
        v_new_json := row_to_json(NEW)::jsonb;
        v_reg_id := NEW.id::text;
    ELSIF (TG_OP = 'DELETE') THEN
        v_old_json := row_to_json(OLD)::jsonb;
        v_reg_id := OLD.id::text;
    END IF;

    INSERT INTO "logs_auditoria" (
        "usuario_id",
        "tabla_afectada",
        "registro_id",
        "accion",
        "valores_anteriores",
        "valores_nuevos",
        "fecha"
    ) VALUES (
        NULL, -- trigger no conoce usuario HTTP por defecto
        TG_TABLE_NAME,
        v_reg_id,
        TG_OP,
        v_old_json,
        v_new_json,
        CURRENT_TIMESTAMP
    );

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;


-- ==========================================
-- TRIGGERS DE AUDITORIA EN TABLAS CRITICAS
-- ==========================================
CREATE TRIGGER trg_auditoria_usuarios
AFTER INSERT OR UPDATE OR DELETE ON "usuarios"
FOR EACH ROW EXECUTE FUNCTION fn_registrar_auditoria();

CREATE TRIGGER trg_auditoria_vehiculos
AFTER INSERT OR UPDATE OR DELETE ON "vehiculos"
FOR EACH ROW EXECUTE FUNCTION fn_registrar_auditoria();

CREATE TRIGGER trg_auditoria_zonas
AFTER INSERT OR UPDATE OR DELETE ON "zonas"
FOR EACH ROW EXECUTE FUNCTION fn_registrar_auditoria();

CREATE TRIGGER trg_auditoria_accesos_vehiculares
AFTER INSERT OR UPDATE OR DELETE ON "accesos_vehiculares"
FOR EACH ROW EXECUTE FUNCTION fn_registrar_auditoria();


-- ==========================================
-- FUNCION Y TRIGGER DE INMUTABILIDAD
-- ==========================================
CREATE OR REPLACE FUNCTION fn_logs_auditoria_inmutable()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'logs_auditoria es append-only: operacion % no permitida', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_logs_auditoria_inmutable
BEFORE UPDATE OR DELETE ON "logs_auditoria"
FOR EACH ROW EXECUTE FUNCTION fn_logs_auditoria_inmutable();
