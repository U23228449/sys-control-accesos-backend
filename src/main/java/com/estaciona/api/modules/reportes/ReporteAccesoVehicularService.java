package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio para la generación de reportes de acceso vehicular y exportación a Excel.
 */
public interface ReporteAccesoVehicularService {

    /**
     * Genera un reporte de accesos vehiculares paginado y filtrado de forma dinámica.
     */
    Page<AccesoVehicularReporteProjection> generarReporte(ReporteAccesoFiltroRequest filtro, Pageable pageable);

    /**
     * Exporta el listado completo filtrado a formato Excel en un arreglo de bytes.
     */
    byte[] exportarExcel(ReporteAccesoFiltroRequest filtro);
}
