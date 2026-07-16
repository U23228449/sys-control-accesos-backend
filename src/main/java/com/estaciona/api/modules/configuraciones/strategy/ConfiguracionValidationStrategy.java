package com.estaciona.api.modules.configuraciones.strategy;

/**
 * Contrato para las estrategias de validación de valores de configuración (HU-017).
 * Cada implementación valida un tipo diferente de valor.
 */
public interface ConfiguracionValidationStrategy {

    /**
     * Valida el valor propuesto para una clave de configuración.
     *
     * @param clave  clave de la configuración.
     * @param valor  nuevo valor propuesto.
     * @throws com.estaciona.api.common.exception.BusinessRuleException si el valor es inválido.
     */
    void validar(String clave, String valor);
}
