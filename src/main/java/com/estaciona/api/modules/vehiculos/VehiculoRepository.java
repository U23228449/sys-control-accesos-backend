package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para Vehiculo.
 */
public interface VehiculoRepository extends JpaRepository<Vehiculo, UUID> {

    /** Verifica si ya existe un vehículo con esa placa (case-insensitive). */
    boolean existsByPlacaIgnoreCase(String placa);

    /** Busca un vehículo por su placa (case-insensitive). */
    java.util.Optional<Vehiculo> findByPlacaIgnoreCase(String placa);

    /** Devuelve todos los vehículos de un usuario. Útil para tests y futuras HUs. */
    List<Vehiculo> findByUsuarioId(UUID usuarioId);

    /** Devuelve todos los vehículos habilitados de un usuario proyectados a VehiculoResumenProjection. */
    List<com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection> findByUsuarioIdAndEnabledTrue(UUID usuarioId);

    /** Busca un vehículo por su placa proyectado a VehiculoBuscadoProjection con datos de propietario. */
    @org.springframework.data.jpa.repository.Query("SELECT v.id as id, v.tipo as tipo, v.placa as placa, v.marcaModelo as marcaModelo, " +
            "v.color as color, v.enabled as enabled, " +
            "u.nombreCompleto as propietarioNombre, u.documento as propietarioDocumento, u.correo as propietarioCorreo " +
            "FROM Vehiculo v JOIN v.usuario u WHERE UPPER(v.placa) = UPPER(:placa)")
    java.util.Optional<com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection> findBuscadoByPlacaIgnoreCase(
            @org.springframework.data.repository.query.Param("placa") String placa);
}
