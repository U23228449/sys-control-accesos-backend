package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Valida que la zona esté activa y cuente con aforo disponible.
 */
@Component
public class ZonaActivaConAforoStrategy implements AccesoVehicularValidationStrategy {

    @Override
    public void validar(Vehiculo vehiculo, Zona zona) {
        if (!"activa".equalsIgnoreCase(zona.getEstado())) {
            throw new BusinessRuleException("La zona de estacionamiento está cerrada.");
        }
        if (zona.getAforoDisponible() <= 0) {
            throw new BusinessRuleException("La zona de estacionamiento no cuenta con aforo disponible.");
        }
    }
}
