package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.zonas.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/zonas-estacionamiento")
@Tag(name = "Zonas de Estacionamiento", description = "Endpoints para consulta de zonas de estacionamiento por Administrador")
@SecurityRequirement(name = "bearerAuth")
public class ZonaEstacionamientoController {

    private final ZonaEstacionamientoService zonaEstacionamientoService;

    public ZonaEstacionamientoController(ZonaEstacionamientoService zonaEstacionamientoService) {
        this.zonaEstacionamientoService = zonaEstacionamientoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar zonas de estacionamiento",
               description = "Lista y filtra zonas de estacionamiento con paginación. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<Page<ZonaEstacionamientoResponseDTO>>> consultarZonas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer campusId,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        ZonaFiltroRequest filtro = new ZonaFiltroRequest(campusId, estado, capacidadMinima, nombre);
        Page<ZonaEstacionamientoResumenProjection> page =
                zonaEstacionamientoService.consultarZonasEstacionamiento(filtro, pageable);

        Page<ZonaEstacionamientoResponseDTO> responsePage = page.map(z -> new ZonaEstacionamientoResponseDTO(
                z.getId(),
                z.getNombre(),
                z.getAforoMaximo(),
                z.getAforoDisponible(),
                z.getEstado()
        ));

        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }
}
