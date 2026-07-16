package com.estaciona.api.modules.vehiculos.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Valida la descripción del color (máximo 30 caracteres, no vacío y formato válido).
 */
@Component
public class ColorUpdateValidationStrategy implements VehiculoUpdateValidationStrategy {

    @Override
    public void validar(VehiculoUpdateRequestDTO request) {
        String color = request.color();
        if (color == null || color.trim().isEmpty()) {
            throw new BusinessRuleException("El color no puede estar vacío.");
        }
        if (color.length() > 30) {
            throw new BusinessRuleException("El color no puede superar los 30 caracteres.");
        }
        // Validación del formato de color (solo letras y espacios permitidos)
        if (!color.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            throw new BusinessRuleException("El color debe contener únicamente caracteres alfabéticos.");
        }
    }
}
