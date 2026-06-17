package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import com.estaciona.api.modules.reportes.entity.AccesoVehicularReporte;
import com.estaciona.api.modules.reportes.spec.AccesoVehicularReporteSpecifications;
import com.estaciona.api.modules.reportes.strategy.ExcelExportacionStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del servicio de reportes de acceso vehicular.
 */
@Service
public class ReporteAccesoVehicularServiceImpl implements ReporteAccesoVehicularService {

    private final AccesoVehicularReporteRepository repository;
    private final ExcelExportacionStrategy excelExportacionStrategy;

    public ReporteAccesoVehicularServiceImpl(AccesoVehicularReporteRepository repository,
                                             ExcelExportacionStrategy excelExportacionStrategy) {
        this.repository = repository;
        this.excelExportacionStrategy = excelExportacionStrategy;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccesoVehicularReporteProjection> generarReporte(ReporteAccesoFiltroRequest filtro, Pageable pageable) {
        Specification<AccesoVehicularReporte> spec = AccesoVehicularReporteSpecifications.construir(filtro);
        return repository.findBy(spec, q -> q.as(AccesoVehicularReporteProjection.class).page(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarExcel(ReporteAccesoFiltroRequest filtro) {
        Specification<AccesoVehicularReporte> spec = AccesoVehicularReporteSpecifications.construir(filtro);
        List<AccesoVehicularReporteProjection> datos = repository.findBy(spec, q -> q.as(AccesoVehicularReporteProjection.class).all());
        return excelExportacionStrategy.exportar(datos);
    }
}
