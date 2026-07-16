package com.estaciona.api.modules.vehiculos.dto;

import java.util.UUID;

/**
 * DTO de respuesta para un vehículo registrado o actualizado.
 */
public record VehiculoResponseDTO(
        UUID id,
        String tipo,
        String placa,
        String marcaModelo,
        String color,
        boolean enabled,
        PropietarioResumen propietario
) {
    /**
     * Resumen del propietario del vehículo.
     */
    public record PropietarioResumen(
            UUID id,
            String nombreCompleto
    ) {}
}
