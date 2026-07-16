package com.estaciona.api.modules.configuraciones.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/**
 * Valida que el valor sea un entero positivo.
 * Usada para configuraciones numéricas como JWT_EXPIRATION_MINUTES, MAX_INTENTOS_LOGIN.
 */
@Component
public class ConfiguracionNumericaStrategy implements ConfiguracionValidationStrategy {

    @Override
    public void validar(String clave, String valor) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero <= 0) {
                throw new BusinessRuleException(
                        "El valor de '" + clave + "' debe ser un entero positivo. Recibido: " + valor);
            }
        } catch (NumberFormatException e) {
            throw new BusinessRuleException(
                    "El valor de '" + clave + "' debe ser un entero positivo. Recibido: '" + valor + "'.");
        }
    }
}
