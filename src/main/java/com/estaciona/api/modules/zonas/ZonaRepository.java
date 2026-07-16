package com.estaciona.api.modules.zonas;

import com.estaciona.api.modules.zonas.dto.ZonaResumenProjection;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Repositorio de acceso a datos para Zona.
 */
public interface ZonaRepository extends JpaRepository<Zona, Integer>, JpaSpecificationExecutor<Zona> {

    /** Valida unicidad de nombre dentro del campus (case-insensitive). */
    boolean existsByCampusIdAndNombreIgnoreCase(Integer campusId, String nombre);

    /** Valida unicidad de nombre en el campus excluyendo la zona actual (para updates). */
    boolean existsByCampusIdAndNombreIgnoreCaseAndIdNot(Integer campusId, String nombre, Integer id);
}
