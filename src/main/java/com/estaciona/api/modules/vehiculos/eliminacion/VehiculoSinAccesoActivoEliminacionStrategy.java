package com.estaciona.api.modules.vehiculos.eliminacion;

import com.estaciona.api.common.exception.ConflictException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida que el vehículo no tenga un acceso en curso antes de eliminarlo.
 * Un vehículo con acceso "en_curso" no puede ser deshabilitado.
 */
@Component
public class VehiculoSinAccesoActivoEliminacionStrategy implements VehiculoEliminacionValidationStrategy {

    @Override
    public void validar(Vehiculo vehiculo, UUID usuarioId, AccesoVehicularRepository accesoRepo) {
        boolean tieneAccesoActivo = accesoRepo.findByVehiculoIdAndEstado(vehiculo.getId(), "en_curso").isPresent();
        if (tieneAccesoActivo) {
            throw new ConflictException(
                    "El vehículo tiene un acceso vehicular en curso y no puede ser eliminado. " +
                    "Registre la salida antes de eliminar el vehículo.");
        }
    }
}
