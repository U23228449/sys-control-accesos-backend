package com.estaciona.api.modules.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de un vehículo (HU-008).
 * La placa es inmutable — no se incluye en este request.
 */
public record VehiculoUpdateRequestDTO(

        @NotBlank(message = "El tipo de vehículo es obligatorio.")
        @Pattern(regexp = "^(auto|moto)$", message = "El tipo debe ser 'auto' o 'moto'.")
        String tipo,

        @NotBlank(message = "La marca y modelo son obligatorios.")
        @Size(max = 100, message = "La marca y modelo no puede superar los 100 caracteres.")
        String marcaModelo,

        @NotBlank(message = "El color es obligatorio.")
        @Size(max = 30, message = "El color no puede superar los 30 caracteres.")
        String color
) {}
