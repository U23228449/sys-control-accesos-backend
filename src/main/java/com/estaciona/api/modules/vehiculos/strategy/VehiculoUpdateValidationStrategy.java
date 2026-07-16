package com.estaciona.api.modules.vehiculos.strategy;

import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;

/**
 * Interfaz para las estrategias de validación de actualización de vehículos.
 */
public interface VehiculoUpdateValidationStrategy {
    void validar(VehiculoUpdateRequestDTO request);
}
