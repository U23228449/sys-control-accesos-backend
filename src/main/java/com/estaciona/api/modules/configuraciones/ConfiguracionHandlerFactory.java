package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionBooleanStrategy;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionNumericaStrategy;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionTemporalStrategy;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionTextoStrategy;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionValidationStrategy;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Factory Method que resuelve la strategy de validación correcta para cada clave de configuración.
 * Extiende con nuevas claves aquí sin tocar el servicio (Open/Closed Principle).
 */
@Component
public class ConfiguracionHandlerFactory {

    private static final Set<String> CLAVES_NUMERICAS = Set.of(
            "JWT_EXPIRATION_MINUTES",
            "MAX_INTENTOS_LOGIN"
    );

    private static final Set<String> CLAVES_BOOLEANAS = Set.of(
            "REGISTRO_PUBLICO_HABILITADO"
    );

    private static final Set<String> CLAVES_TEMPORALES = Set.of(
            "SESSION_TIMEOUT"
    );

    private final ConfiguracionNumericaStrategy numericaStrategy;
    private final ConfiguracionBooleanStrategy booleanStrategy;
    private final ConfiguracionTemporalStrategy temporalStrategy;
    private final ConfiguracionTextoStrategy textoStrategy;

    public ConfiguracionHandlerFactory(ConfiguracionNumericaStrategy numericaStrategy,
                                       ConfiguracionBooleanStrategy booleanStrategy,
                                       ConfiguracionTemporalStrategy temporalStrategy,
                                       ConfiguracionTextoStrategy textoStrategy) {
        this.numericaStrategy = numericaStrategy;
        this.booleanStrategy = booleanStrategy;
        this.temporalStrategy = temporalStrategy;
        this.textoStrategy = textoStrategy;
    }

    /**
     * Resuelve la strategy de validación adecuada para la clave dada.
     * Si la clave no está clasificada, retorna la strategy de texto (permisiva).
     *
     * @param clave clave de la configuración en UPPER_SNAKE_CASE.
     * @return strategy de validación correspondiente.
     */
    public ConfiguracionValidationStrategy resolver(String clave) {
        if (CLAVES_NUMERICAS.contains(clave)) {
            return numericaStrategy;
        }
        if (CLAVES_BOOLEANAS.contains(clave)) {
            return booleanStrategy;
        }
        if (CLAVES_TEMPORALES.contains(clave)) {
            return temporalStrategy;
        }
        return textoStrategy;
    }
}
