package com.estaciona.api.modules.zonas.dto;

import java.time.OffsetDateTime;

/**
 * Proyección JPA para el listado paginado de zonas (HU-011).
 * Solo expone los campos necesarios para el listado, sin cargar todos los datos.
 */
public interface ZonaResumenProjection {
    Integer getId();
    String getCampusNombre();
    String getNombre();
    String getUbicacion();
    String getTipo();
    Integer getAforoMaximo();
    Integer getAforoDisponible();
    String getEstado();
    Boolean getEnabled();
    java.time.Instant getCreatedAt();
}
