package com.estaciona.api.modules.usuarios.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección de interfaz JPA para el listado resumido de usuarios.
 * Usada en GET /api/v1/usuarios con filtros y paginación.
 *
 * Nota: getRolNombre() se resuelve mediante alias JPQL en @Query del repositorio
 * (u.rol.nombre as rolNombre) ya que es una propiedad anidada.
 */
public interface UsuarioResumenProjection {

    UUID getId();

    String getNombreCompleto();

    String getCorreo();

    String getDocumento();

    /** Nombre del rol (ADMINISTRADOR, SEGURIDAD, etc.). */
    String getRolNombre();

    default String getRol() {
        return getRolNombre();
    }

    String getTipoUsuario();

    Boolean getEnabled();

    java.time.Instant getCreatedAt();

    Integer getCampusId();

    Integer getZonaId();

    String getTipoGuardia();
}
