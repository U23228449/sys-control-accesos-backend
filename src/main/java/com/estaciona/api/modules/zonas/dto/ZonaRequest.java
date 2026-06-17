package com.estaciona.api.modules.zonas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear una zona de estacionamiento.
 */
public record ZonaRequest(

        @NotNull(message = "El ID del campus es obligatorio.")
        Integer campusId,

        @NotBlank(message = "El nombre de la zona es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @Size(max = 150, message = "La ubicación no puede superar los 150 caracteres.")
        String ubicacion,

        @NotBlank(message = "El tipo de zona es obligatorio.")
        String tipo,

        @NotNull(message = "El aforo máximo es obligatorio.")
        @Positive(message = "El aforo máximo debe ser un número positivo.")
        Integer aforoMaximo
) {}
