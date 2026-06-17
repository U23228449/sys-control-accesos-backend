package com.estaciona.api.modules.auditoria.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta con el detalle completo de un evento de auditoría.
 */
public record AuditoriaEventoResponse(
        UUID id,
        String usuario, // nombreCompleto del usuario (o null si fue trigger)
        String tablaAfectada,
        String registroId,
        String accion,
        String valoresAnteriores,
        String valoresNuevos,
        OffsetDateTime fecha
) {}
