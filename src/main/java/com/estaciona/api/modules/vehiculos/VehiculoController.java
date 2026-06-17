package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
     * El propietario se obtiene del claim 'sub' del JWT — nunca del body.
     */
    @PostMapping
    @Operation(
            summary = "Registrar vehículo",
            description = "Registra un nuevo vehículo. El propietario es el usuario autenticado. " +
                          "Tipos aceptados: 'auto', 'moto'. Formato de placa: 6-8 caracteres alfanuméricos."
    )
    public ResponseEntity<ApiResponse<VehiculoResponse>> registrarVehiculo(
            @Valid @RequestBody VehiculoRequest request) {

        // El usuarioId viene del JWT (SecurityContext), nunca del body
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
    @org.springframework.web.bind.annotation.GetMapping("/me")
    @Operation(
            summary = "Consultar mis vehículos",
            description = "Devuelve los vehículos habilitados del usuario autenticado en el token."
    )
    public ResponseEntity<ApiResponse<java.util.List<com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection>>> consultarMisVehiculos() {
        var usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        java.util.List<com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection> response = 
                vehiculoService.consultarMisVehiculos(usuarioId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Busca un vehículo por su placa (restringido a personal de seguridad).
     */
    @org.springframework.web.bind.annotation.GetMapping("/buscar/{placa}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(
            summary = "Buscar vehículo por placa",
            description = "Busca un vehículo por su placa y expone datos del propietario. Restringido a rol SEGURIDAD."
    )
    public ResponseEntity<ApiResponse<com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection>> buscarPorPlaca(
            @org.springframework.web.bind.annotation.PathVariable("placa") String placa) {
        com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection response = vehiculoService.buscarPorPlaca(placa);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
