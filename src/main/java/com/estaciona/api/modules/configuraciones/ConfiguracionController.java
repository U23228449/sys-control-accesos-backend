package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints para gestión de configuraciones del sistema (HU-017).
 * Rol requerido: ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/configuraciones")
@Tag(name = "Configuraciones", description = "Gestión de parámetros de configuración del sistema")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    /**
     * Lista todas las configuraciones habilitadas del sistema.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar configuraciones",
               description = "Devuelve todas las configuraciones activas del sistema. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<List<ConfiguracionResponse>>> listarConfiguraciones() {
        List<ConfiguracionResponse> configs = configuracionService.listarConfiguraciones();
        return ResponseEntity.ok(ApiResponse.ok(configs));
    }

    /**
     * Actualiza el valor de una configuración específica por su clave. (HU-017)
     */
    @PatchMapping("/{clave}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar configuración",
               description = "Actualiza el valor de un parámetro del sistema validando el formato según la clave. " +
                             "Publica un evento de cambio para notificar a otros componentes. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<ConfiguracionResponse>> actualizarConfiguracion(
            @PathVariable String clave,
            @Valid @RequestBody ConfiguracionUpdateRequest request) {
        ConfiguracionResponse response = configuracionService.actualizarConfiguracion(clave, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Configuración '" + clave + "' actualizada correctamente."));
    }
}
