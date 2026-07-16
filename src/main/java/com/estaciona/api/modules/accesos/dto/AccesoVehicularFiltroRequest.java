package com.estaciona.api.modules.accesos.dto;

import java.time.OffsetDateTime;

/**
 * Parámetros de filtro para el historial de accesos vehiculares (HU-015).
 * Todos los campos son opcionales.
 */
public record AccesoVehicularFiltroRequest(
        OffsetDateTime desde,
        OffsetDateTime hasta,
        String placa,
        String estado,
        Integer zonaId
) {}
