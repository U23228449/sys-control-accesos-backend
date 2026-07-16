package com.estaciona.api.modules.vehiculos.eliminacion;

import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;

import java.util.UUID;

/**
 * Contrato para las estrategias de validación antes de eliminar un vehículo (soft delete).
 */
public interface VehiculoEliminacionValidationStrategy {

    void validar(Vehiculo vehiculo, UUID usuarioId, AccesoVehicularRepository accesoRepo);
}
