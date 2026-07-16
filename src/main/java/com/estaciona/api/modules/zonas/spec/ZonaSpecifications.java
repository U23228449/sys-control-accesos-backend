package com.estaciona.api.modules.zonas.spec;

import com.estaciona.api.modules.zonas.dto.ZonaFiltroRequest;
import com.estaciona.api.modules.zonas.entity.Zona;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificaciones JPA para filtrado dinámico de Zonas.
 * Patrón Strategy para la construcción de criterios de búsqueda.
 */
public final class ZonaSpecifications {

    private ZonaSpecifications() {
        // Clase utilitaria - no instanciar
    }

    public static Specification<Zona> porCampus(Integer campusId) {
        return (root, query, cb) ->
                cb.equal(root.get("campus").get("id"), campusId);
    }

    public static Specification<Zona> porEstado(String estado) {
        return (root, query, cb) ->
                cb.equal(root.get("estado"), estado);
    }

    public static Specification<Zona> conCapacidadMinima(Integer capacidadMinima) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("aforoMaximo"), capacidadMinima);
    }

    public static Specification<Zona> porNombre(String nombre) {
        return (root, query, cb) -> {
            String patron = "%" + nombre.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("nombre")), patron);
        };
    }

    public static Specification<Zona> soloHabilitadas() {
        return (root, query, cb) ->
                cb.equal(root.get("enabled"), true);
    }

    /**
     * Construye una especificación combinada basada en el DTO de filtro.
     */
    public static Specification<Zona> construir(ZonaFiltroRequest filtro) {
        Specification<Zona> spec = soloHabilitadas();

        if (filtro.campusId() != null) {
            spec = spec.and(porCampus(filtro.campusId()));
        }
        if (filtro.estado() != null && !filtro.estado().isBlank()) {
            spec = spec.and(porEstado(filtro.estado()));
        }
        if (filtro.capacidadMinima() != null) {
            spec = spec.and(conCapacidadMinima(filtro.capacidadMinima()));
        }
        if (filtro.nombre() != null && !filtro.nombre().isBlank()) {
            spec = spec.and(porNombre(filtro.nombre()));
        }

        return spec;
    }
}
