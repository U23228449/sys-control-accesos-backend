package com.estaciona.api.modules.zonas;

import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;

/**
 * Contrato del servicio de zonas de estacionamiento.
 */
public interface ZonaService {

    ZonaResponse crearZona(ZonaRequest request);
}
