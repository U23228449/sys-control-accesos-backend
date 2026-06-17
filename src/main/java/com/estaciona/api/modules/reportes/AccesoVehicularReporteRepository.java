package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.reportes.entity.AccesoVehicularReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio JPA para la consulta del reporte de accesos vehiculares.
 */
@Repository
public interface AccesoVehicularReporteRepository 
        extends JpaRepository<AccesoVehicularReporte, UUID>, JpaSpecificationExecutor<AccesoVehicularReporte> {
}
