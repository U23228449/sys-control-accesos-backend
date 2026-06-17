package com.estaciona.api.modules.campus;

import com.estaciona.api.modules.campus.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de Campus — solo lectura en HU-010.
 */
public interface CampusRepository extends JpaRepository<Campus, Integer> {
}
