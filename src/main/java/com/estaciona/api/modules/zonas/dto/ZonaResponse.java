package com.estaciona.api.modules.zonas.dto;

import java.time.Instant;

/**
 * DTO de respuesta para una zona de estacionamiento.
 */
public record ZonaResponse(
        Integer id,
        Integer campusId,
        String campusNombre,
        String nombre,
        String ubicacion,
        String tipo,
        Integer aforoMaximo,
        Integer aforoDisponible,
        String estado,
        boolean enabled,
        Instant createdAt
) {}
