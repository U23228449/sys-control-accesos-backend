package com.estaciona.api.modules.reportes;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import com.estaciona.api.modules.reportes.dto.ZonaDisponibilidadProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Endpoints para consulta y exportación de reportes.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Endpoints de reportes y exportación")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('COORDINADOR_SEGURIDAD')")
public class ReporteController {

    private final ReporteAccesoVehicularService service;
    private final ReporteZonaServiceImpl zonaService;

    public ReporteController(ReporteAccesoVehicularService service,
                             ReporteZonaServiceImpl zonaService) {
        this.service = service;
        this.zonaService = zonaService;
    }

    /**
     * Devuelve el reporte de accesos paginado y filtrado. (HU-018)
     */
    @GetMapping("/accesos")
    @Operation(summary = "Listar y filtrar accesos vehiculares para reporte",
               description = "Devuelve los registros de acceso vehicular de forma paginada y filtrable. Requiere rol COORDINADOR_SEGURIDAD.")
    public ResponseEntity<ApiResponse<Page<AccesoVehicularReporteProjection>>> generarReporte(
            ReporteAccesoFiltroRequest filtro,
            @PageableDefault(sort = "horaIngreso", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccesoVehicularReporteProjection> response = service.generarReporte(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Exporta el listado de accesos al formato solicitado (Excel/PDF). (HU-018)
     */
    @GetMapping("/accesos/exportar")
    @Operation(summary = "Exportar reporte de accesos",
               description = "Genera y descarga un archivo en el formato solicitado (Excel '.xlsx' o PDF '.pdf'). Requiere rol COORDINADOR_SEGURIDAD.")
    public ResponseEntity<byte[]> exportarReporte(
            ReporteAccesoFiltroRequest filtro,
            @RequestParam(value = "formato", defaultValue = "excel") String formato) {
        byte[] bytes = service.exportarReporte(filtro, formato);
        var estrategia = service.obtenerEstrategia(formato);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "reporte-accesos-" + timestamp + estrategia.getExtensionArchivo();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(estrategia.getContentType()));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * Devuelve el reporte de disponibilidad de zonas en JSON. (HU-019)
     */
    @GetMapping("/zonas")
    @Operation(summary = "Reporte de disponibilidad de zonas",
               description = "Lista la disponibilidad actual de todas las zonas habilitadas. Requiere rol COORDINADOR_SEGURIDAD.")
    public ResponseEntity<ApiResponse<List<ZonaDisponibilidadProjection>>> reporteZonas() {
        List<ZonaDisponibilidadProjection> zonas = zonaService.obtenerDisponibilidad();
        return ResponseEntity.ok(ApiResponse.ok(zonas));
    }

    /**
     * Exporta el reporte de zonas como archivo Excel. (HU-019)
     */
    @GetMapping("/zonas/exportar")
    @Operation(summary = "Exportar reporte de zonas como Excel",
               description = "Genera y descarga un archivo Excel con la disponibilidad de zonas. Requiere rol COORDINADOR_SEGURIDAD.")
    public ResponseEntity<byte[]> exportarReporteZonas() {
        byte[] bytes = zonaService.exportarExcel();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "reporte-zonas-" + timestamp + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
