package com.estaciona.api.modules.roles;

import com.estaciona.api.modules.roles.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de Rol — usado en tests de integración para buscar roles por nombre.
 */
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
}
