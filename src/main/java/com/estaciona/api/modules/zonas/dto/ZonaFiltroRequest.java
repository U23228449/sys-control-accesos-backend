package com.estaciona.api.modules.zonas.dto;

/**
 * Parámetros de filtro para el listado paginado de zonas (HU-011).
 * Todos los campos son opcionales.
 */
public record ZonaFiltroRequest(
        Integer campusId,
        String estado,
        Integer capacidadMinima,
        String nombre
) {}
