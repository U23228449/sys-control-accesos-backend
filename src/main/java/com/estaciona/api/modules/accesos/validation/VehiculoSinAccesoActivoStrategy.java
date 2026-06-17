package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Valida que el vehículo no tenga un acceso activo ("en_curso") en el sistema.
 */
@Component
public class VehiculoSinAccesoActivoStrategy implements AccesoVehicularValidationStrategy {

    private final AccesoVehicularRepository accesoVehicularRepository;

    public VehiculoSinAccesoActivoStrategy(AccesoVehicularRepository accesoVehicularRepository) {
        this.accesoVehicularRepository = accesoVehicularRepository;
    }

    @Override
    public void validar(Vehiculo vehiculo, Zona zona) {
        accesoVehicularRepository.findByVehiculoIdAndEstado(vehiculo.getId(), "en_curso")
                .ifPresent(acceso -> {
                    throw new BusinessRuleException("El vehículo ya tiene un acceso en curso.");
                });
    }
}
