package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponseDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoDesvinculadoResponseDTO;
import com.estaciona.api.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de gestión de vehículos.
 * Requiere autenticación (cualquier rol). El propietario se resuelve del JWT.
 */
@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Registro y gestión de vehículos")
@SecurityRequirement(name = "bearerAuth")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    /**
     * Registra un vehículo para el usuario autenticado.
     */
    @PostMapping
    @Operation(summary = "Registrar vehículo",
               description = "Registra un nuevo vehículo. El propietario es el usuario autenticado.")
    public ResponseEntity<ApiResponse<VehiculoResponse>> registrarVehiculo(
            @Valid @RequestBody VehiculoRequest request) {
        var usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        VehiculoResponse response = vehiculoService.registrarVehiculo(request, usuarioId);
        URI location = URI.create("/api/v1/vehiculos/" + response.id());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(response, "Vehículo registrado correctamente."));
    }

    /**
     * Consulta los vehículos del usuario autenticado.
     */
    @GetMapping("/me")
    @Operation(summary = "Consultar mis vehículos",
               description = "Devuelve los vehículos habilitados del usuario autenticado en el token.")
    public ResponseEntity<ApiResponse<List<VehiculoResumenProjection>>> consultarMisVehiculos() {
        var usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        List<VehiculoResumenProjection> response = vehiculoService.consultarMisVehiculos(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Busca un vehículo por su placa (restringido a personal de seguridad).
     */
    @GetMapping("/buscar/{placa}")
    @PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(summary = "Buscar vehículo por placa",
               description = "Busca un vehículo por su placa y expone datos del propietario. Rol requerido: SEGURIDAD.")
    public ResponseEntity<ApiResponse<VehiculoBuscadoProjection>> buscarPorPlaca(
            @PathVariable String placa) {
        VehiculoBuscadoProjection response = vehiculoService.buscarPorPlaca(placa);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Actualiza tipo, marca/modelo y color del vehículo propio. La placa es inmutable. (HU-008)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'PROFESOR', 'PERSONAL_ADMINISTRATIVO')")
    @Operation(summary = "Actualizar vehículo",
               description = "Modifica tipo, marca/modelo y color. La placa no puede cambiarse. Solo el propietario.")
    public ResponseEntity<ApiResponse<VehiculoResponseDTO>> actualizarVehiculo(
            @PathVariable UUID id,
            @Valid @RequestBody VehiculoUpdateRequestDTO request) {
        UUID usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        VehiculoResponseDTO response = vehiculoService.actualizarVehiculo(id, usuarioId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Vehículo actualizado correctamente."));
    }

    /**
     * Elimina (soft delete) el vehículo del usuario autenticado. (HU-009)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'PROFESOR', 'PERSONAL_ADMINISTRATIVO')")
    @Operation(summary = "Eliminar vehículo",
               description = "Deshabilita el vehículo (soft delete). Solo el propietario puede hacerlo.")
    public ResponseEntity<ApiResponse<VehiculoDesvinculadoResponseDTO>> eliminarVehiculo(@PathVariable UUID id) {
        UUID usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        VehiculoDesvinculadoResponseDTO response = vehiculoService.eliminarVehiculo(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Vehículo desvinculado correctamente."));
    }

    /**
     * Habilita (reactiva) el vehículo del usuario autenticado.
     */
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'PROFESOR', 'PERSONAL_ADMINISTRATIVO')")
    @Operation(summary = "Reactivar vehículo",
               description = "Habilita el vehículo (reactivar). Solo el propietario puede hacerlo.")
    public ResponseEntity<ApiResponse<VehiculoResponseDTO>> reactivarVehiculo(@PathVariable UUID id) {
        UUID usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        VehiculoResponseDTO response = vehiculoService.reactivarVehiculo(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Vehículo reactivado correctamente."));
    }
}
