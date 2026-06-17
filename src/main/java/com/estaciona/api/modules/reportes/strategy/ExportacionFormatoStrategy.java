package com.estaciona.api.modules.reportes.strategy;

import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;

import java.util.List;

/**
 * Estrategia de formato para la exportación de reportes de accesos vehiculares.
 */
public interface ExportacionFormatoStrategy {

    /**
     * Convierte el listado de registros al formato de la estrategia concreta.
     */
    byte[] exportar(List<AccesoVehicularReporteProjection> datos);

    /**
     * Retorna el tipo de contenido HTTP (MIME type) asociado al formato.
     */
    String getContentType();

    /**
     * Retorna la extensión del archivo (con punto incluido, ej: ".xlsx").
     */
    String getExtensionArchivo();
}
