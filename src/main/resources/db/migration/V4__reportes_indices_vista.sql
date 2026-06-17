-- Crear índices compuestos para optimizar el reporte
CREATE INDEX idx_accesos_zona_estado_fecha
  ON accesos_vehiculares (zona_id, estado, hora_ingreso);

CREATE INDEX idx_accesos_hora_salida
  ON accesos_vehiculares (hora_salida);

-- Crear vista transaccional para simplificar el reporte
CREATE OR REPLACE VIEW vw_reporte_accesos_vehiculares AS
SELECT
    a.id AS id,
    v.placa AS placa,
    v.tipo AS tipo_vehiculo,
    v.marca_modelo AS marca_modelo,
    u_prop.nombre_completo AS propietario,
    z.nombre AS zona_nombre,
    c.nombre AS campus_nombre,
    u_ge.nombre_completo AS guardia_entrada_nombre,
    u_gs.nombre_completo AS guardia_salida_nombre,
    a.hora_ingreso AS hora_ingreso,
    a.hora_salida AS hora_salida,
    a.estado AS estado,
    a.zona_id AS zona_id,
    a.guardia_entrada_id AS guardia_entrada_id,
    z.tipo AS tipo_zona
FROM accesos_vehiculares a
JOIN vehiculos v ON a.vehiculo_id = v.id
JOIN usuarios u_prop ON v.usuario_id = u_prop.id
JOIN zonas z ON a.zona_id = z.id
JOIN campus c ON z.campus_id = c.id
JOIN usuarios u_ge ON a.guardia_entrada_id = u_ge.id
LEFT JOIN usuarios u_gs ON a.guardia_salida_id = u_gs.id;
