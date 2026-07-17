package com.estaciona.api.modules.usuarios.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para un usuario registrado.
 * Nunca expone la contraseña hasheada.
 */
public record UsuarioResponse(
        UUID id,
        String nombreCompleto,
        String correo,
        String documento,
        String rol,
        String tipoUsuario,
        boolean enabled,
        Instant createdAt,
        Integer campusId,
        Integer zonaId,
        String tipoGuardia
) {}
