package com.estaciona.api.modules.configuraciones.strategy;

import org.springframework.stereotype.Component;

/**
 * Estrategia de texto libre: sin restricciones de formato.
 * Es el fallback por defecto cuando ninguna otra strategy aplica.
 */
@Component
public class ConfiguracionTextoStrategy implements ConfiguracionValidationStrategy {

    @Override
    public void validar(String clave, String valor) {
        // Sin restricciones — cualquier texto no vacío es válido
        // La validación @NotBlank del DTO ya garantiza que no es vacío
    }
}
