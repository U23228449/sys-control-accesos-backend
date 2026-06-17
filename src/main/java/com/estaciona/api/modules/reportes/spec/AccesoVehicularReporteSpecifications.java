package com.estaciona.api.modules.reportes.spec;

import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import com.estaciona.api.modules.reportes.entity.AccesoVehicularReporte;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Especificaciones JPA para realizar consultas y filtros dinámicos sobre AccesoVehicularReporte.
 */
public final class AccesoVehicularReporteSpecifications {

    private AccesoVehicularReporteSpecifications() {
        // Constructor privado para evitar instanciación
    }

    public static Specification<AccesoVehicularReporte> porZona(Integer zonaId) {
        return (root, query, cb) -> cb.equal(root.get("zonaId"), zonaId);
    }

    public static Specification<AccesoVehicularReporte> porGarita(UUID garitaId) {
        return (root, query, cb) -> cb.equal(root.get("guardiaEntradaId"), garitaId);
    }

    public static Specification<AccesoVehicularReporte> porEstado(String estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<AccesoVehicularReporte> porTipoZona(String tipoZona) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("tipoZona")), tipoZona.toLowerCase());
    }

    public static Specification<AccesoVehicularReporte> porTipoVehiculo(String tipoVehiculo) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("tipoVehiculo")), tipoVehiculo.toLowerCase());
    }

    public static Specification<AccesoVehicularReporte> porRangoFechasIngreso(OffsetDateTime desde, OffsetDateTime hasta) {
        return (root, query, cb) -> {
            if (desde != null && hasta != null) {
                return cb.between(root.get("horaIngreso"), desde, hasta);
            } else if (desde != null) {
                return cb.greaterThanOrEqualTo(root.get("horaIngreso"), desde);
            } else if (hasta != null) {
                return cb.lessThanOrEqualTo(root.get("horaIngreso"), hasta);
            }
            return cb.conjunction();
        };
    }

    /**
     * Construye una especificación combinada basada en todos los filtros opcionales de la petición.
     */
    public static Specification<AccesoVehicularReporte> construir(ReporteAccesoFiltroRequest filtro) {
        Specification<AccesoVehicularReporte> spec = Specification.where(null);

        if (filtro == null) {
            return spec;
        }

        if (filtro.zonaId() != null) {
            spec = spec.and(porZona(filtro.zonaId()));
        }
        if (filtro.garitaId() != null) {
            spec = spec.and(porGarita(filtro.garitaId()));
        }
        if (filtro.estado() != null && !filtro.estado().isBlank()) {
            spec = spec.and(porEstado(filtro.estado()));
        }
        if (filtro.tipoZona() != null && !filtro.tipoZona().isBlank()) {
            spec = spec.and(porTipoZona(filtro.tipoZona()));
        }
        if (filtro.tipoVehiculo() != null && !filtro.tipoVehiculo().isBlank()) {
            spec = spec.and(porTipoVehiculo(filtro.tipoVehiculo()));
        }
        if (filtro.desde() != null || filtro.hasta() != null) {
            spec = spec.and(porRangoFechasIngreso(filtro.desde(), filtro.hasta()));
        }

        return spec;
    }
}
