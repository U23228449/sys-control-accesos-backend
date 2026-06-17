package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Endpoints para gestión de zonas de estacionamiento.
 * Rol requerido: ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/zonas-estacionamiento")
@Tag(name = "Zonas", description = "Gestión de zonas de estacionamiento")
@SecurityRequirement(name = "bearerAuth")
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    /**
     * Registra una nueva zona de estacionamiento dentro de un campus.
     * Solo accesible por usuarios con rol ADMINISTRADOR.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar zona", description = "Crea una nueva zona de estacionamiento. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<ZonaResponse>> crearZona(
            @Valid @RequestBody ZonaRequest request) {
        ZonaResponse zona = zonaService.crearZona(request);
        URI location = URI.create("/api/v1/zonas-estacionamiento/" + zona.id());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(zona, "Zona registrada correctamente."));
    }
}
