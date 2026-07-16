package com.estaciona.api.modules.vehiculos.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Valida la longitud de la descripción de la marca y modelo (máximo 100 caracteres).
 */
@Component
public class MarcaModeloUpdateValidationStrategy implements VehiculoUpdateValidationStrategy {

    @Override
    public void validar(VehiculoUpdateRequestDTO request) {
        String marcaModelo = request.marcaModelo();
        if (marcaModelo == null || marcaModelo.trim().isEmpty()) {
            throw new BusinessRuleException("La marca y modelo no pueden estar vacíos.");
        }
        if (marcaModelo.length() > 100) {
            throw new BusinessRuleException("La marca y modelo no pueden superar los 100 caracteres.");
        }
    }
}
