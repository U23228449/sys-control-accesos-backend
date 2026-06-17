package com.estaciona.api.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para el endpoint de login.
 * El campo 'identificador' acepta tanto correo como documento.
 */
public record LoginRequest(

        @NotBlank(message = "El identificador (correo o documento) es obligatorio.")
        String identificador,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {}
