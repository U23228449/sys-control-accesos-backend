package com.estaciona.api.modules.auditoria;

import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResponse;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResumenProjection;
import com.estaciona.api.modules.auditoria.dto.AuditoriaFiltroRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato para el servicio de auditoría.
 */
public interface AuditoriaService {

    /**
     * Registra manualmente un evento de auditoría.
     */
    AuditoriaEventoResponse registrarEvento(AuditoriaEventoRequest request, UUID usuarioId);

    /**
     * Obtiene todos los eventos de auditoría de forma paginada y ligera (proyección).
     */
    Page<AuditoriaEventoResumenProjection> listarEventos(Pageable pageable);

    /**
     * Filtra los eventos de auditoría dinámicamente según filtros provistos.
     */
    Page<AuditoriaEventoResumenProjection> filtrarEventos(AuditoriaFiltroRequest filtro, Pageable pageable);

    /**
     * Obtiene el detalle completo de un evento de auditoría por su ID.
     *
     * @throws com.estaciona.api.common.exception.ResourceNotFoundException si no existe.
     */
    AuditoriaEventoResponse obtenerDetalle(UUID id);
}
