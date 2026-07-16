package com.estaciona.api.modules.configuraciones.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/**
 * Valida que el valor sea "true" o "false" (case-insensitive).
 */
@Component
public class ConfiguracionBooleanStrategy implements ConfiguracionValidationStrategy {

    @Override
    public void validar(String clave, String valor) {
        if (!valor.equalsIgnoreCase("true") && !valor.equalsIgnoreCase("false")) {
            throw new BusinessRuleException(
                    "El valor de '" + clave + "' debe ser 'true' o 'false'. Recibido: '" + valor + "'.");
        }
    }
}
