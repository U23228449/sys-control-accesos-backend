package com.estaciona.api.modules.auditoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar manualmente un evento de auditoría.
 */
public record AuditoriaEventoRequest(
        @NotBlank(message = "La tabla afectada es obligatoria.")
        String tablaAfectada,

        @NotBlank(message = "El ID de registro es obligatorio.")
        @Size(max = 36, message = "El ID de registro no puede superar los 36 caracteres.")
        String registroId,

        @NotBlank(message = "La acción es obligatoria.")
        @Pattern(
                regexp = "^(INSERT|UPDATE|DELETE)$",
                message = "La acción debe ser INSERT, UPDATE o DELETE."
        )
        String accion,

        String valoresAnteriores, // JSON serializado, opcional

        String valoresNuevos // JSON serializado, opcional
) {}
