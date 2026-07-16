package com.estaciona.api.modules.usuarios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para que el ADMINISTRADOR cambie el estado (habilitado/deshabilitado) de un usuario.
 */
public record UsuarioUpdateEstadoRequest(

        @NotNull(message = "El campo enabled es obligatorio.")
        Boolean enabled
) {}
