package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.modules.configuraciones.entity.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para Configuracion.
 */
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {

    /** Busca una configuración activa por su clave única. */
    Optional<Configuracion> findByClaveAndEnabledTrue(String clave);
}
