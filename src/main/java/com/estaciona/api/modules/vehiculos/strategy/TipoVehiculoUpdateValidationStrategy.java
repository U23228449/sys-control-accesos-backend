package com.estaciona.api.modules.vehiculos.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Valida que el tipo de vehículo sea 'auto' o 'moto'.
 */
@Component
public class TipoVehiculoUpdateValidationStrategy implements VehiculoUpdateValidationStrategy {

    @Override
    public void validar(VehiculoUpdateRequestDTO request) {
        String tipo = request.tipo();
        if (tipo == null || (!tipo.equalsIgnoreCase("auto") && !tipo.equalsIgnoreCase("moto"))) {
            throw new BusinessRuleException("El tipo de vehículo debe ser 'auto' o 'moto'.");
        }
    }
}
