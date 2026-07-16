package com.estaciona.api.modules.usuarios.spec;

import com.estaciona.api.modules.usuarios.dto.UsuarioFiltroRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificaciones JPA para filtrado dinámico de Usuario.
 * Patrón Strategy: cada método produce un Specification que se combina en {@link #construir}.
 */
public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
        // Clase utilitaria — no instanciar
    }

    /**
     * Filtra por nombre exacto del rol.
     *
     * @param rolNombre nombre del rol (ej.: "ADMINISTRADOR")
     */
    public static Specification<Usuario> porRol(String rolNombre) {
        return (root, query, cb) ->
                cb.equal(root.get("rol").get("nombre"), rolNombre);
    }

    /**
     * Filtra por estado enabled del usuario.
     *
     * @param enabled true = activo, false = deshabilitado
     */
    public static Specification<Usuario> porEstado(Boolean enabled) {
        return (root, query, cb) ->
                cb.equal(root.get("enabled"), enabled);
    }

    /**
     * Búsqueda de texto parcial (case-insensitive) en nombreCompleto, correo o documento.
     * Usa OR para los tres campos.
     *
     * @param texto texto a buscar
     */
    public static Specification<Usuario> porBusqueda(String texto) {
        return (root, query, cb) -> {
            String patron = "%" + texto.toLowerCase() + "%";
            CriteriaBuilder.In<String> unused = null; // evita import no utilizado
            Predicate porNombre = cb.like(cb.lower(root.get("nombreCompleto")), patron);
            Predicate porCorreo  = cb.like(cb.lower(root.get("correo")), patron);
            Predicate porDoc     = cb.like(cb.lower(root.get("documento")), patron);
            return cb.or(porNombre, porCorreo, porDoc);
        };
    }

    /**
     * Combina los filtros del request en una sola Specification.
     * Solo incluye un filtro si el campo no es nulo ni vacío.
     *
     * @param filtro DTO con los parámetros de filtro
     * @return Specification combinada con AND
     */
    public static Specification<Usuario> construir(UsuarioFiltroRequest filtro) {
        Specification<Usuario> spec = Specification.where(null);

        if (filtro.rolNombre() != null && !filtro.rolNombre().isBlank()) {
            spec = spec.and(porRol(filtro.rolNombre()));
        }
        if (filtro.enabled() != null) {
            spec = spec.and(porEstado(filtro.enabled()));
        }
        if (filtro.busqueda() != null && !filtro.busqueda().isBlank()) {
            spec = spec.and(porBusqueda(filtro.busqueda()));
        }

        return spec;
    }
}
