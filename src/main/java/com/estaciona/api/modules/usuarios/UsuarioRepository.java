package com.estaciona.api.modules.usuarios;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para Usuario.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca un usuario por correo o documento. Usado en login y en CustomUserDetailsService.
     */
    Optional<Usuario> findByCorreoOrDocumento(String correo, String documento);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByDocumento(String documento);
}
