package com.estaciona.api.modules.vehiculos.dto;

import java.util.UUID;

/**
 * Proyección de Spring Data JPA para listado ligero de vehículos del usuario autenticado.
 */
public interface VehiculoResumenProjection {
    UUID getId();
    String getTipo();
    String getPlaca();
    String getMarcaModelo();
    String getColor();
    Boolean getEnabled();
}
