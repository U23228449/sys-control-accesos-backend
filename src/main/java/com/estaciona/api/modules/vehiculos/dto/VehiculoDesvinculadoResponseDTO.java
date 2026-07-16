package com.estaciona.api.modules.vehiculos.dto;

import java.util.UUID;

/**
 * DTO que confirma la desvinculación (soft delete) de un vehículo y expone sus datos básicos.
 */
public record VehiculoDesvinculadoResponseDTO(
        String mensaje,
        UUID id,
        String placa,
        String marcaModelo,
        String color,
        String propietarioNombre
) {}
