package com.estaciona.api.modules.reportes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección de Spring Data JPA para la vista de reporte de accesos vehiculares.
 */
public interface AccesoVehicularReporteProjection {
    UUID getId();
    String getPlaca();
    String getTipoVehiculo();
    String getMarcaModelo();
    String getPropietario();
    String getZonaNombre();
    String getCampusNombre();
    String getGuardiaEntradaNombre();
    String getGuardiaSalidaNombre();
    OffsetDateTime getHoraIngreso();
    OffsetDateTime getHoraSalida();
    String getEstado();
}
