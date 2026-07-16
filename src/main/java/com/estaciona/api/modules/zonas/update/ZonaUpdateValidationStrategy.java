package com.estaciona.api.modules.zonas.update;

import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.dto.ZonaUpdateRequest;
import com.estaciona.api.modules.zonas.entity.Zona;

/**
 * Estrategia de validación para actualización de zonas de estacionamiento.
 * Cada implementación valida un aspecto diferente del request antes de persistir.
 */
public interface ZonaUpdateValidationStrategy {

    /**
     * Ejecuta la validación sobre la zona actual y los datos del request.
     *
     * @param zonaActual     entidad existente en base de datos.
     * @param request        datos nuevos enviados por el cliente.
     * @param zonaRepository repositorio de zonas para consultas de unicidad.
     * @throws com.estaciona.api.common.exception.DuplicateResourceException si hay duplicados.
     * @throws com.estaciona.api.common.exception.BusinessRuleException       si se viola una regla de negocio.
     */
    void validar(Zona zonaActual, ZonaUpdateRequest request, ZonaRepository zonaRepository);
}
