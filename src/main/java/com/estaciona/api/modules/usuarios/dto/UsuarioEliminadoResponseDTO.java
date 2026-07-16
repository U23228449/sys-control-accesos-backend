package com.estaciona.api.modules.usuarios.dto;

import java.util.UUID;

/**
 * DTO que confirma la deshabilitación/eliminación de un usuario y expone sus datos básicos.
 */
public record UsuarioEliminadoResponseDTO(
        String mensaje,
        UUID id,
        String nombreCompleto,
        String correo,
        String documento,
        String rol
) {}
