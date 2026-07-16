package com.estaciona.api.modules.vehiculos.eliminacion;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida que el vehículo esté habilitado antes de intentar eliminarlo.
 */
@Component
public class VehiculoExistenteEliminacionStrategy implements VehiculoEliminacionValidationStrategy {

    @Override
    public void validar(Vehiculo vehiculo, UUID usuarioId, AccesoVehicularRepository accesoRepo) {
        if (!vehiculo.isEnabled()) {
            throw new BusinessRuleException("El vehículo ya está deshabilitado.");
        }
    }
}
