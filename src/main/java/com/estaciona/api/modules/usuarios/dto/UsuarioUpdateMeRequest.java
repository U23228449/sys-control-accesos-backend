package com.estaciona.api.modules.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para que el usuario autenticado actualice sus propios datos personales.
 * No incluye rolId ni enabled — el usuario no puede cambiar su propio rol ni desactivarse.
 */
public record UsuarioUpdateMeRequest(

        @NotBlank(message = "El nombre completo es obligatorio.")
        @Size(max = 150, message = "El nombre completo no puede superar los 150 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correo,

        @NotBlank(message = "El documento es obligatorio.")
        @Size(max = 20, message = "El documento no puede superar los 20 caracteres.")
        String documento,

        /** Requerido solo si se desea cambiar la contraseña. Validado en el Service. */
        String passwordActual,

        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres.")
        String passwordNuevo
) {}
