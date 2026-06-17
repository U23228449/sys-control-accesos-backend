package com.estaciona.api.modules.accesos;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;
import com.estaciona.api.security.SecurityContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

/**
 * Controlador REST para gestionar el ingreso y la salida de vehículos.
 * Acceso restringido al rol SEGURIDAD.
 */
@RestController
@RequestMapping("/api/v1/accesos-vehiculares")
@Tag(name = "Accesos Vehiculares", description = "Control de ingresos y salidas en garita")
@SecurityRequirement(name = "bearerAuth")
public class AccesoVehicularController {

    private final AccesoVehicularService accesoVehicularService;

    public AccesoVehicularController(AccesoVehicularService accesoVehicularService) {
        this.accesoVehicularService = accesoVehicularService;
    }

    /**
     * Registra el ingreso de un vehículo.
     */
    @PostMapping
    @PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(
            summary = "Registrar ingreso vehicular",
            description = "Permite a un guardia registrar el ingreso de un vehículo válido. " +
                          "Decrementa el aforo de la zona elegida. Requiere rol SEGURIDAD."
    )
    public ResponseEntity<ApiResponse<AccesoVehicularResponse>> registrarIngreso(
            @Valid @RequestBody AccesoVehicularRequest request) {

        UUID guardiaId = SecurityContextUtils.obtenerUsuarioIdActual();
        AccesoVehicularResponse response = accesoVehicularService.registrarIngreso(request, guardiaId);
        URI location = URI.create("/api/v1/accesos-vehiculares/" + response.id());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(response, "Ingreso vehicular registrado correctamente."));
    }

    /**
     * Registra la salida de un vehículo.
     */
    @PatchMapping("/{id}/salida")
    @PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(
            summary = "Registrar salida vehicular",
            description = "Permite a un guardia registrar la salida de un vehículo con un acceso en curso. " +
                          "Incrementa el aforo de la zona. Requiere rol SEGURIDAD."
    )
    public ResponseEntity<ApiResponse<AccesoVehicularResponse>> registrarSalida(
            @PathVariable("id") UUID accesoId) {

        UUID guardiaSalidaId = SecurityContextUtils.obtenerUsuarioIdActual();
        AccesoVehicularResponse response = accesoVehicularService.registrarSalida(accesoId, guardiaSalidaId);

        return ResponseEntity
                .ok()
                .body(ApiResponse.ok(response, "Salida vehicular registrada correctamente."));
    }
}
