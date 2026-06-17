package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;

/**
 * Estrategia de validación para el registro de accesos vehiculares.
 * Sigue el patrón Strategy.
 */
public interface AccesoVehicularValidationStrategy {

    /**
     * Valida las reglas de negocio para el vehículo y la zona indicados.
     *
     * @param vehiculo vehículo que intenta ingresar.
     * @param zona     zona a la que se intenta ingresar.
     * @throws RuntimeException si se viola alguna regla de negocio.
     */
    void validar(Vehiculo vehiculo, Zona zona);
}
