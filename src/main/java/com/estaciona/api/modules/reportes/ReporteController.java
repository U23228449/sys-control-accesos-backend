package com.estaciona.api.modules.reportes;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.reportes.dto.AccesoVehicularReporteProjection;
import com.estaciona.api.modules.reportes.dto.ReporteAccesoFiltroRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Endpoints para consulta y exportación de reportes de acceso vehicular.
 */
@RestController
@RequestMapping("/api/v1/reportes/accesos")
@Tag(name = "Reportes", description = "Endpoints de reportes y exportación")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('COORDINADOR_SEGURIDAD')")
public class ReporteController {

    private final ReporteAccesoVehicularService service;

    public ReporteController(ReporteAccesoVehicularService service) {
        this.service = service;
    }

    /**
     * Devuelve el reporte de accesos paginado y filtrado.
     */
    @GetMapping
    @Operation(
            summary = "Listar y filtrar accesos vehiculares para reporte",
            description = "Devuelve los registros de acceso vehicular de forma paginada y filtrable. Requiere rol COORDINADOR_SEGURIDAD."
    )
    public ResponseEntity<ApiResponse<Page<AccesoVehicularReporteProjection>>> generarReporte(
            ReporteAccesoFiltroRequest filtro,
            @PageableDefault(sort = "horaIngreso", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AccesoVehicularReporteProjection> response = service.generarReporte(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Exporta el listado de accesos filtrado a formato Excel (.xlsx).
     */
    @GetMapping("/exportar")
    @Operation(
            summary = "Exportar reporte de accesos a Excel",
            description = "Genera y descarga un archivo Excel (.xlsx) con los accesos vehiculares filtrados. Requiere rol COORDINADOR_SEGURIDAD."
    )
    public ResponseEntity<byte[]> exportarExcel(ReporteAccesoFiltroRequest filtro) {
        byte[] excelBytes = service.exportarExcel(filtro);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "reporte-accesos-" + timestamp + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment").filename(filename).build());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
