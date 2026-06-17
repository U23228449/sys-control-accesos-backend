package com.estaciona.api.modules.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para registrar un nuevo usuario.
 */
public record UsuarioRequest(
        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(max = 150, message = "El nombre completo no puede superar los 150 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El formato del correo es inválido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correo,

        @NotBlank(message = "El documento es obligatorio.")
        @Size(max = 20, message = "El documento no puede superar los 20 caracteres.")
        String documento,

        @NotNull(message = "El rol es obligatorio.")
        Integer rolId,

        @Pattern(
                regexp = "^(alumno|docente|personal_admin)$",
                message = "El tipo de usuario debe ser 'alumno', 'docente' o 'personal_admin'."
        )
        String tipoUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password
) {}
