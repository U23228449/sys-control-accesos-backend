package com.estaciona.api.modules.reportes.dto;

import java.math.BigDecimal;

/**
 * Proyección JPA para el reporte de disponibilidad de zonas (HU-019).
 * Mapea la vista vw_reporte_zonas_disponibilidad.
 */
public interface ZonaDisponibilidadProjection {
    Integer getId();
    String getCampusNombre();
    String getZonaNombre();
    String getUbicacion();
    String getTipo();
    Integer getAforoMaximo();
    Integer getAforoDisponible();
    Integer getAforoOcupado();
    BigDecimal getPorcentajeOcupacion();
    String getEstado();
}
