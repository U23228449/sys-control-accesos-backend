package com.estaciona.api.modules.zonas;

import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a datos para Zona.
 */
public interface ZonaRepository extends JpaRepository<Zona, Integer> {

    /** Valida unicidad de nombre dentro del campus (case-insensitive). */
    boolean existsByCampusIdAndNombreIgnoreCase(Integer campusId, String nombre);
}
