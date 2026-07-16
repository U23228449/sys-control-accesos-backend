package com.estaciona.api.modules.zonas.dto;

/**
 * DTO de respuesta para retornar campos relevantes de una zona (HU-011).
 */
public record ZonaEstacionamientoResponseDTO(
        Integer id,
        String nombre,
        Integer aforoTotal,
        Integer espaciosDisponibles,
        String estado
) {}
