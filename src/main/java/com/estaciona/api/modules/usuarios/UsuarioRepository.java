package com.estaciona.api.modules.usuarios;

import com.estaciona.api.modules.usuarios.dto.UsuarioResumenProjection;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para Usuario.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>,
        JpaSpecificationExecutor<Usuario> {

    /**
     * Busca un usuario por correo o documento. Usado en login y en CustomUserDetailsService.
     */
    Optional<Usuario> findByCorreoOrDocumento(String correo, String documento);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDocumento(String documento);

    /** Valida unicidad de correo excluyendo al usuario actual (case-insensitive). */
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, UUID id);

    /** Valida unicidad de documento excluyendo al usuario actual. */
    boolean existsByDocumentoAndIdNot(String documento, UUID id);

    /** Cuenta los administradores activos del sistema. Usado para proteger al último admin. */
    long countByRolNombreAndEnabledTrue(String rolNombre);

    /** Valida si ya existe un guardia con el mismo tipo asignado a la zona. */
    boolean existsByZonaIdAndTipoGuardiaIgnoreCaseAndEnabledTrue(Integer zonaId, String tipoGuardia);

    /**
     * Lista todos los usuarios con proyección resumida (sin filtros).
     * El alias 'rolNombre' mapea u.rol.nombre a {@link UsuarioResumenProjection#getRolNombre()}.
     */
    @Query("SELECT u.id as id, u.nombreCompleto as nombreCompleto, u.correo as correo, " +
           "u.documento as documento, u.rol.nombre as rolNombre, u.tipoUsuario as tipoUsuario, " +
           "u.enabled as enabled, u.createdAt as createdAt, u.campus.id as campusId, " +
           "u.zona.id as zonaId, u.tipoGuardia as tipoGuardia FROM Usuario u")
    Page<UsuarioResumenProjection> findAllProjected(Pageable pageable);
}
