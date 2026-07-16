package com.estaciona.api.modules.reportes;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import com.estaciona.api.modules.reportes.entity.AccesoVehicularReporte;
import com.estaciona.api.modules.reportes.spec.AccesoVehicularReporteSpecifications;
import com.estaciona.api.modules.reportes.strategy.ExportacionFormatoStrategy;
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
    private final List<ExportacionFormatoStrategy> estrategias;

    public ReporteAccesoVehicularServiceImpl(AccesoVehicularReporteRepository repository,
                                             List<ExportacionFormatoStrategy> estrategias) {
        this.repository = repository;
        this.estrategias = estrategias;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccesoVehicularReporteProjection> generarReporte(ReporteAccesoFiltroRequest filtro, Pageable pageable) {
        Specification<AccesoVehicularReporte> spec = AccesoVehicularReporteSpecifications.construir(filtro);
        return repository.findBy(spec, q -> q.as(AccesoVehicularReporteProjection.class).page(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarReporte(ReporteAccesoFiltroRequest filtro, String formato) {
        Specification<AccesoVehicularReporte> spec = AccesoVehicularReporteSpecifications.construir(filtro);
        List<AccesoVehicularReporteProjection> datos = repository.findBy(spec, q -> q.as(AccesoVehicularReporteProjection.class).all());
        
        ExportacionFormatoStrategy estrategia = obtenerEstrategia(formato);
        return estrategia.exportar(datos);
    }

    @Override
    public ExportacionFormatoStrategy obtenerEstrategia(String formato) {
        if (formato == null || (!"pdf".equalsIgnoreCase(formato) && !"excel".equalsIgnoreCase(formato) && !"xlsx".equalsIgnoreCase(formato))) {
            throw new BusinessRuleException("Formato de exportación no soportado: " + formato);
        }
        String extensionBuscada = "pdf".equalsIgnoreCase(formato) ? ".pdf" : ".xlsx";
        return estrategias.stream()
                .filter(est -> est.getExtensionArchivo().equalsIgnoreCase(extensionBuscada))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Formato de exportación no soportado: " + formato));
    }
}
