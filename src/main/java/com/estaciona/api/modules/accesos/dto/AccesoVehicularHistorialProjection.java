package com.estaciona.api.modules.accesos.dto;

import java.time.OffsetDateTime;

/**
 * Proyección JPA para el historial paginado de accesos vehiculares (HU-015).
 */
public interface AccesoVehicularHistorialProjection {
    java.util.UUID getId();
    String getPlaca();
    String getTipoVehiculo();
    String getMarcaModelo();
    String getPropietarioNombre();
    String getZonaNombre();
    String getCampusNombre();
    String getGuardiaEntradaNombre();
    String getGuardiaSalidaNombre(); // puede ser null
    OffsetDateTime getHoraIngreso();
    OffsetDateTime getHoraSalida(); // puede ser null
    String getEstado();

    default Long getTiempoPermanenciaCalculado() {
        if (getHoraIngreso() == null || getHoraSalida() == null) {
            return null;
        }
        return java.time.Duration.between(getHoraIngreso(), getHoraSalida()).toMinutes();
    }
}
