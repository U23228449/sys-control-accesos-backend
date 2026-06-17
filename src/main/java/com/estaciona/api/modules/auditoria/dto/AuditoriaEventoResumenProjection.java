package com.estaciona.api.modules.auditoria.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección de Spring Data JPA para listados livianos de auditoría.
 */
public interface AuditoriaEventoResumenProjection {
    UUID getId();
    String getTablaAfectada();
    String getRegistroId();
    String getAccion();
    OffsetDateTime getFecha();
}
