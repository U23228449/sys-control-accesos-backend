package com.estaciona.api.modules.zonas.dto;

/**
 * Proyección JPA para recuperar desde PostgreSQL únicamente los campos requeridos
 * por la vista de distribución y capacidad (HU-011).
 */
public interface ZonaEstacionamientoResumenProjection {
    Integer getId();
    String getNombre();
    Integer getAforoMaximo();
    Integer getAforoDisponible();
    String getEstado();
}
