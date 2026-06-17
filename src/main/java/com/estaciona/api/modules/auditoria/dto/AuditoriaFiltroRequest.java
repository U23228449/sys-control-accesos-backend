package com.estaciona.api.modules.auditoria.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Parámetros de consulta para el filtrado dinámico de logs de auditoría.
 */
public record AuditoriaFiltroRequest(
        String tablaAfectada,
        String accion,
        UUID usuarioId,
        OffsetDateTime desde,
        OffsetDateTime hasta
) {}
