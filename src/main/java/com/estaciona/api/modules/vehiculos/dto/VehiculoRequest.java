package com.estaciona.api.modules.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar un vehículo.
 * El propietario NO se incluye aquí — se resuelve del JWT en el Controller.
 */
public record VehiculoRequest(

        @NotBlank(message = "El tipo de vehículo es obligatorio.")
        @Pattern(
                regexp = "^(?i)(auto|moto)$",
                message = "El tipo debe ser 'auto' o 'moto'."
        )
        String tipo,

        @NotBlank(message = "La placa es obligatoria.")
        @Size(max = 15, message = "La placa no puede superar los 15 caracteres.")
        @Pattern(
                regexp = "^[A-Za-z0-9\\-]{6,8}$",
                message = "La placa debe tener entre 6 y 8 caracteres alfanuméricos o guiones."
        )
        String placa,

        @NotBlank(message = "La marca/modelo es obligatoria.")
        @Size(max = 100, message = "La marca/modelo no puede superar los 100 caracteres.")
        String marcaModelo,

        @NotBlank(message = "El color es obligatorio.")
        @Size(max = 30, message = "El color no puede superar los 30 caracteres.")
        String color
) {}
