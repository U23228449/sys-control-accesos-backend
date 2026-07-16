package com.estaciona.api.modules.vehiculos.eliminacion;

import com.estaciona.api.common.exception.ForbiddenOperationException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida que el usuario autenticado sea el propietario del vehículo.
 */
@Component
public class VehiculoPropietarioEliminacionStrategy implements VehiculoEliminacionValidationStrategy {

    @Override
    public void validar(Vehiculo vehiculo, UUID usuarioId, AccesoVehicularRepository accesoRepo) {
        if (!vehiculo.getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenOperationException("No tiene permisos para eliminar este vehículo.");
        }
    }
}
