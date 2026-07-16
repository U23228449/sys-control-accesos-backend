package com.estaciona.api.modules.zonas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para el cambio de estado operativo de una zona de estacionamiento.
 */
public record ZonaUpdateEstadoRequest(

        @NotBlank(message = "El estado es obligatorio.")
        @Pattern(regexp = "^(activa|cerrada)$",
                 message = "El estado debe ser 'activa' o 'cerrada'.")
        String estado
) {}
