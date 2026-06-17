package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.ForbiddenOperationException;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Valida que el vehículo esté habilitado en el sistema.
 */
@Component
public class VehiculoHabilitadoStrategy implements AccesoVehicularValidationStrategy {

    @Override
    public void validar(Vehiculo vehiculo, Zona zona) {
        if (!vehiculo.isEnabled()) {
            throw new ForbiddenOperationException("Vehículo deshabilitado. No está autorizado a ingresar al campus.");
        }
    }
}
