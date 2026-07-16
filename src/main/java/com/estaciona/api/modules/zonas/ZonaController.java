package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.zonas.dto.ZonaFiltroRequest;
import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResumenProjection;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;
import com.estaciona.api.modules.zonas.dto.ZonaUpdateEstadoRequest;
import com.estaciona.api.modules.zonas.dto.ZonaUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Endpoints para gestión de zonas de estacionamiento.
 * Rol requerido: ADMINISTRADOR (salvo indicación específica).
 */
@RestController
@RequestMapping("/api/v1/zonas")
@Tag(name = "Zonas", description = "Gestión de zonas de estacionamiento")
@SecurityRequirement(name = "bearerAuth")
public class ZonaController {

    private final ZonaEstacionamientoService zonaService;

    public ZonaController(ZonaEstacionamientoService zonaService) {
        this.zonaService = zonaService;
    }

    /**
     * Registra una nueva zona de estacionamiento dentro de un campus.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar zona",
               description = "Crea una nueva zona de estacionamiento. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<ZonaResponse>> crearZona(
            @Valid @RequestBody ZonaRequest request) {
        ZonaResponse zona = zonaService.crearZona(request);
        URI location = URI.create("/api/v1/zonas-estacionamiento/" + zona.id());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(zona, "Zona registrada correctamente."));
    }

    /**
     * Actualiza los datos de una zona existente (HU-012).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar zona",
               description = "Modifica nombre, ubicación, tipo y aforo de una zona. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<ZonaResponse>> actualizarZona(
            @PathVariable Integer id,
            @Valid @RequestBody ZonaUpdateRequest request) {
        ZonaResponse zona = zonaService.actualizarZona(id, request);
        return ResponseEntity.ok(ApiResponse.ok(zona, "Zona actualizada correctamente."));
    }

    /**
     * Cambia el estado operativo de una zona (activa/cerrada) (HU-012).
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado de zona",
               description = "Transiciona una zona entre 'activa' y 'cerrada'. No se puede cerrar con vehículos dentro.")
    public ResponseEntity<ApiResponse<ZonaResponse>> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody(required = false) @Valid ZonaUpdateEstadoRequest request) {
        ZonaResponse zona = zonaService.actualizarEstado(id, request);
        return ResponseEntity.ok(ApiResponse.ok(zona, "Estado de zona actualizado correctamente."));
    }

    /**
     * Lista y filtra zonas habilitadas con paginación (HU-011).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SEGURIDAD', 'COORDINADOR_SEGURIDAD')")
    @Operation(summary = "Consultar zonas",
               description = "Lista las zonas habilitadas con filtros opcionales.")
    public ResponseEntity<ApiResponse<?>> consultarZonas(
            @ModelAttribute ZonaFiltroRequest filtro,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            jakarta.servlet.http.HttpServletRequest request) {
        if (request.getRequestURI().contains("/api/v1/zonas") && !request.getRequestURI().contains("/api/v1/zonas-estacionamiento")) {
            java.util.List<ZonaResumenProjection> zonas = zonaService.consultarZonas(filtro);
            return ResponseEntity.ok(ApiResponse.ok(zonas));
        } else if (request.getParameter("page") == null && request.getParameter("size") == null) {
            java.util.List<ZonaResumenProjection> zonas = zonaService.consultarZonas(filtro);
            return ResponseEntity.ok(ApiResponse.ok(zonas));
        } else {
            Page<ZonaResumenProjection> zonas = zonaService.consultarZonas(filtro, pageable);
            return ResponseEntity.ok(ApiResponse.ok(zonas));
        }
    }
}
