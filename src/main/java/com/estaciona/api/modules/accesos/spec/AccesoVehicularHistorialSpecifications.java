package com.estaciona.api.modules.accesos.spec;

import com.estaciona.api.modules.accesos.dto.AccesoVehicularFiltroRequest;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import java.time.OffsetDateTime;

public class AccesoVehicularHistorialSpecifications {

    public static Specification<AccesoVehicular> porEstado(String estado) {
        return (root, query, cb) -> estado == null || estado.isBlank() ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<AccesoVehicular> porPlaca(String placa) {
        return (root, query, cb) -> {
            if (placa == null || placa.isBlank()) return null;
            Join<Object, Object> vehiculo = root.join("vehiculo");
            return cb.like(cb.lower(vehiculo.get("placa")), "%" + placa.toLowerCase() + "%");
        };
    }

    public static Specification<AccesoVehicular> porZona(Integer zonaId) {
        return (root, query, cb) -> zonaId == null ? null : cb.equal(root.get("zona").get("id"), zonaId);
    }

    public static Specification<AccesoVehicular> porRangoFechas(OffsetDateTime desde, OffsetDateTime hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null) return null;
            if (desde != null && hasta != null) {
                return cb.between(root.get("horaIngreso"), desde, hasta);
            }
            if (desde != null) {
                return cb.greaterThanOrEqualTo(root.get("horaIngreso"), desde);
            }
            return cb.lessThanOrEqualTo(root.get("horaIngreso"), hasta);
        };
    }

    public static Specification<AccesoVehicular> construir(AccesoVehicularFiltroRequest filtro) {
        return Specification.where(porEstado(filtro.estado()))
                .and(porPlaca(filtro.placa()))
                .and(porZona(filtro.zonaId()))
                .and(porRangoFechas(filtro.desde(), filtro.hasta()));
    }
}
