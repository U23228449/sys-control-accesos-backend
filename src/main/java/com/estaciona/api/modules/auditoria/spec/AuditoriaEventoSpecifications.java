package com.estaciona.api.modules.auditoria.spec;

import com.estaciona.api.modules.auditoria.dto.AuditoriaFiltroRequest;
import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Filtros dinámicos para logs de auditoría utilizando el patrón Specification de JPA.
 */
public class AuditoriaEventoSpecifications {

    private AuditoriaEventoSpecifications() {
        // No instanciar
    }

    public static Specification<LogAuditoria> porTablaAfectada(String tabla) {
        return (root, query, cb) -> tabla == null || tabla.trim().isEmpty() ? null
                : cb.equal(cb.lower(root.get("tablaAfectada")), tabla.trim().toLowerCase());
    }

    public static Specification<LogAuditoria> porAccion(String accion) {
        return (root, query, cb) -> accion == null || accion.trim().isEmpty() ? null
                : cb.equal(cb.upper(root.get("accion")), accion.trim().toUpperCase());
    }

    public static Specification<LogAuditoria> porUsuarioId(UUID usuarioId) {
        return (root, query, cb) -> usuarioId == null ? null
                : cb.equal(root.get("usuario").get("id"), usuarioId);
    }

    public static Specification<LogAuditoria> porRangoFechas(OffsetDateTime desde, OffsetDateTime hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null) {
                return null;
            }
            if (desde != null && hasta != null) {
                return cb.between(root.get("fecha"), desde, hasta);
            }
            if (desde != null) {
                return cb.greaterThanOrEqualTo(root.get("fecha"), desde);
            }
            return cb.lessThanOrEqualTo(root.get("fecha"), hasta);
        };
    }

    /**
     * Construye una especificación combinada basada en los filtros no nulos.
     */
    public static Specification<LogAuditoria> construir(AuditoriaFiltroRequest filtro) {
        return Specification.where(porTablaAfectada(filtro.tablaAfectada()))
                .and(porAccion(filtro.accion()))
                .and(porUsuarioId(filtro.usuarioId()))
                .and(porRangoFechas(filtro.desde(), filtro.hasta()));
    }
}
