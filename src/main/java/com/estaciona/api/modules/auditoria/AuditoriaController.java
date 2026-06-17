package com.estaciona.api.modules.auditoria;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResponse;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoResumenProjection;
import com.estaciona.api.modules.auditoria.dto.AuditoriaFiltroRequest;
import com.estaciona.api.security.SecurityContextUtils;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Endpoints para consulta y registro de logs de auditoría.
 */
@RestController
@RequestMapping("/api/v1/auditoria/eventos")
@Tag(name = "Auditoría", description = "Endpoints de logs de auditoría del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Registra de forma manual un evento de auditoría.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Registrar evento de auditoría manual",
            description = "Permite registrar manualmente un evento de auditoría. Requiere rol ADMINISTRADOR."
    )
    public ResponseEntity<ApiResponse<AuditoriaEventoResponse>> registrarEvento(
            @Valid @RequestBody AuditoriaEventoRequest request) {

        UUID usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        AuditoriaEventoResponse response = auditoriaService.registrarEvento(request, usuarioId);
        URI location = URI.create("/api/v1/auditoria/eventos/" + response.id());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(response, "Evento de auditoría registrado correctamente."));
    }

    /**
     * Consulta y filtra logs de auditoría de forma paginada.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR_SEGURIDAD')")
    @Operation(
            summary = "Listar y filtrar logs de auditoría",
            description = "Devuelve los logs de auditoría paginados y filtrados. Excluye detalles JSON en el listado para rendimiento."
    )
    public ResponseEntity<ApiResponse<Page<AuditoriaEventoResumenProjection>>> listarEventos(
            AuditoriaFiltroRequest filtro,
            @PageableDefault(sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditoriaEventoResumenProjection> response = auditoriaService.filtrarEventos(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Obtiene el detalle completo de un evento de auditoría por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR_SEGURIDAD')")
    @Operation(
            summary = "Ver detalle de un evento de auditoría",
            description = "Devuelve el detalle completo del evento de auditoría incluyendo los campos JSON de cambios."
    )
    public ResponseEntity<ApiResponse<AuditoriaEventoResponse>> obtenerDetalle(
            @PathVariable("id") UUID id) {

        AuditoriaEventoResponse response = auditoriaService.obtenerDetalle(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
