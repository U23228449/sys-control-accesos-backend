package com.estaciona.api.modules.accesos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para registrar el ingreso de un vehículo.
 */
public record AccesoVehicularRequest(
        @NotBlank(message = "La placa es obligatoria.")
        String placa,

        @NotNull(message = "La zona de estacionamiento es obligatoria.")
        Integer zonaId
) {}
