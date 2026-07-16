-- ==========================================
-- V5: Reporte de Zonas de Estacionamiento
-- ==========================================
-- Índices para mejorar el rendimiento del reporte de zonas (HU-019)

CREATE INDEX IF NOT EXISTS idx_zonas_campus_estado_tipo
    ON zonas (campus_id, estado, tipo);

CREATE INDEX IF NOT EXISTS idx_zonas_aforo_disponible
    ON zonas (aforo_disponible);

-- Vista de disponibilidad en tiempo real para el reporte de zonas
CREATE OR REPLACE VIEW vw_reporte_zonas_disponibilidad AS
SELECT
    z.id,
    c.nombre   AS campus_nombre,
    z.nombre   AS zona_nombre,
    z.ubicacion,
    z.tipo,
    z.aforo_maximo,
    z.aforo_disponible,
    (z.aforo_maximo - z.aforo_disponible) AS aforo_ocupado,
    ROUND(
        ((z.aforo_maximo - z.aforo_disponible)::numeric / NULLIF(z.aforo_maximo, 0)) * 100,
        2
    ) AS porcentaje_ocupacion,
    z.estado,
    z.enabled
FROM zonas z
JOIN campus c ON c.id = z.campus_id
WHERE z.enabled = TRUE;
