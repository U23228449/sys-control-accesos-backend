package com.estaciona.api.modules.vehiculos.dto;

import java.util.UUID;

/**
 * DTO de respuesta para un vehículo registrado.
 */
public record VehiculoResponse(
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
