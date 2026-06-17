package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.excel.ReporteExcelBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Estrategia de exportación concreta para formato Excel (.xlsx).
 */
@Component
public class ExcelExportacionStrategy implements ExportacionFormatoStrategy {

    @Override
    public byte[] exportar(List<AccesoVehicularReporteProjection> datos) {
        return new ReporteExcelBuilder()
                .conTitulo("Reporte de Accesos Vehiculares")
                .conDatos(datos)
                .construir();
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getExtensionArchivo() {
        return ".xlsx";
    }
}
