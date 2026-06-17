package com.estaciona.api.modules.accesos;

import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para acceder a los datos de AccesoVehicular.
 */
public interface AccesoVehicularRepository extends JpaRepository<AccesoVehicular, UUID> {

    /**
     * Busca un acceso vehicular por el ID del vehículo y su estado.
     * Usado para validar si un vehículo tiene un acceso activo ("en_curso").
     */
    Optional<AccesoVehicular> findByVehiculoIdAndEstado(UUID vehiculoId, String estado);
}
