package com.estaciona.api.modules.usuarios.dto;

/**
 * DTO de filtros para la consulta paginada de usuarios.
 * Todos los campos son opcionales.
 *
 * @param rolNombre nombre del rol (ADMINISTRADOR, SEGURIDAD, COORDINADOR_SEGURIDAD, USUARIO)
 * @param enabled   estado habilitado/deshabilitado del usuario
 * @param busqueda  texto parcial a buscar en nombreCompleto, correo o documento
 */
public record UsuarioFiltroRequest(
        String rolNombre,
        Boolean enabled,
        String busqueda
) {}
