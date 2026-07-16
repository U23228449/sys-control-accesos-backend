package com.estaciona.api.modules.usuarios.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para que el ADMINISTRADOR cambie el rol de un usuario.
 */
public record UsuarioUpdateRolRequest(

        @NotNull(message = "El rolId es obligatorio.")
        Integer rolId
) {}
