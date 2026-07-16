package com.estaciona.api.modules.configuraciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para actualizar una configuración del sistema (HU-017).
 */
public record ConfiguracionUpdateRequest(

        @NotBlank(message = "El valor de la configuración no puede estar vacío.")
        @Size(max = 255, message = "El valor no puede superar los 255 caracteres.")
        String valor
) {}
