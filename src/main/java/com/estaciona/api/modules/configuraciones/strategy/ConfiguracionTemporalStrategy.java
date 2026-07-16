package com.estaciona.api.modules.configuraciones.strategy;

import com.estaciona.api.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/**
 * Valida que el valor tenga formato temporal: número seguido de 'm', 'h' o 'd'.
 * Ejemplos válidos: "30m", "1h", "24h", "7d".
 */
@Component
public class ConfiguracionTemporalStrategy implements ConfiguracionValidationStrategy {

    private static final java.util.regex.Pattern PATTERN_TEMPORAL =
            java.util.regex.Pattern.compile("^\\d+(m|h|d)$");

    @Override
    public void validar(String clave, String valor) {
        if (!PATTERN_TEMPORAL.matcher(valor).matches()) {
            throw new BusinessRuleException(
                    "El valor de '" + clave + "' debe tener formato temporal (ej: '30m', '1h', '7d'). " +
                    "Recibido: '" + valor + "'.");
        }
    }
}
