package com.estaciona.api.modules.vehiculos.dto;

import java.util.UUID;

/**
 * Proyección de Spring Data JPA para la búsqueda detallada de un vehículo por placa.
 * Incluye campos del propietario del vehículo.
 */
public interface VehiculoBuscadoProjection {
    UUID getId();
    String getTipo();
    String getPlaca();
    String getMarcaModelo();
    String getColor();
    Boolean getEnabled();
    String getPropietarioNombre();
    String getPropietarioDocumento();
    String getPropietarioCorreo();
}
