package com.estaciona.api.modules.zonas;

import com.estaciona.api.modules.zonas.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato del servicio de zonas de estacionamiento (HU-011).
 */
public interface ZonaEstacionamientoService {

    ZonaResponse crearZona(ZonaRequest request);

    /** Actualiza nombre, aforo y tipo de una zona existente. (HU-012) */
    ZonaResponse actualizarZona(Integer id, ZonaUpdateRequest request);

    /** Cambia el estado operativo de una zona (activa/cerrada). (HU-012) */
    ZonaResponse actualizarEstado(Integer id, ZonaUpdateEstadoRequest request);

    /** Lista y filtra zonas habilitadas con paginación. (HU-011) */
    Page<ZonaResumenProjection> consultarZonas(ZonaFiltroRequest filtro, Pageable pageable);

    /** Lista y filtra zonas habilitadas sin paginación. */
    List<ZonaResumenProjection> consultarZonas(ZonaFiltroRequest filtro);

    /** Consulta paginada específica de zonas para la vista de monitoreo (HU-011). */
    Page<ZonaEstacionamientoResumenProjection> consultarZonasEstacionamiento(ZonaFiltroRequest filtro, Pageable pageable);
}
