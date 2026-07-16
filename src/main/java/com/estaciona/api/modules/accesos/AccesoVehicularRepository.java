package com.estaciona.api.modules.accesos;

import com.estaciona.api.modules.accesos.dto.AccesoVehicularFiltroRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularHistorialProjection;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para acceder a los datos de AccesoVehicular.
 */
public interface AccesoVehicularRepository extends JpaRepository<AccesoVehicular, UUID>,
        JpaSpecificationExecutor<AccesoVehicular> {

    /**
     * Busca un acceso vehicular por el ID del vehículo y su estado.
     * Usado para validar si un vehículo tiene un acceso activo ("en_curso").
     */
    Optional<AccesoVehicular> findByVehiculoIdAndEstado(UUID vehiculoId, String estado);

    /**
     * Verifica si existe un acceso vehicular en curso para un usuario.
     */
    boolean existsByUsuarioIdAndEstado(UUID usuarioId, String estado);

    /**
     * Lista todos los accesos vehiculares que tengan un determinado estado.
     * Usado para listar los accesos activos ("en_curso").
     */
    java.util.List<AccesoVehicular> findByEstadoIgnoreCase(String estado);

}
