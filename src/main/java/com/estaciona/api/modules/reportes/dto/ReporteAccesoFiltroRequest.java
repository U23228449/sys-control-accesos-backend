package com.estaciona.api.modules.reportes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Parámetros de filtro para la generación de reportes de acceso vehicular.
 */
public record ReporteAccesoFiltroRequest(
        OffsetDateTime desde,
        OffsetDateTime hasta,
        Integer zonaId,
        UUID garitaId, // Interpretado como guardiaEntradaId según requisitos
        String estado,
        String tipoZona,
        String tipoVehiculo
) {}
