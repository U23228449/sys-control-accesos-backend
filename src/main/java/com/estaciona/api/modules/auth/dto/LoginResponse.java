package com.estaciona.api.modules.auth.dto;

import java.util.UUID;

/**
 * DTO de respuesta del login exitoso.
 * Contiene el JWT y un resumen del usuario autenticado.
 */
public record LoginResponse(

        String token,
        String tipoToken,
        long expiraEn,          // segundos hasta expiración
        UsuarioResumen usuario
) {

    /**
     * Resumen del usuario incluido en la respuesta de login.
     */
    public record UsuarioResumen(
            UUID id,
            String nombreCompleto,
            String correo,
            String rol,
            Integer campusId,
            Integer zonaId,
            String tipoGuardia
    ) {}
}
