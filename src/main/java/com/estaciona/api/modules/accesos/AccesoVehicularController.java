package com.estaciona.api.modules.accesos;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularFiltroRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularHistorialProjection;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Controlador REST para gestionar el ingreso, la salida y el historial de vehículos.
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
     * Registra el ingreso de un vehículo. (HU-007)
     */
    @PostMapping
    @PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(summary = "Registrar ingreso vehicular",
               description = "Permite a un guardia registrar el ingreso de un vehículo válido. Requiere rol SEGURIDAD.")
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
     * Registra la salida de un vehículo. (HU-007)
     */
    @PatchMapping("/{id}/salida")
    @PreAuthorize("hasRole('SEGURIDAD')")
    @Operation(summary = "Registrar salida vehicular",
               description = "Permite a un guardia registrar la salida de un vehículo con un acceso en curso. Requiere rol SEGURIDAD.")
    public ResponseEntity<ApiResponse<AccesoVehicularResponse>> registrarSalida(
            @PathVariable("id") UUID accesoId) {
        UUID guardiaSalidaId = SecurityContextUtils.obtenerUsuarioIdActual();
        AccesoVehicularResponse response = accesoVehicularService.registrarSalida(accesoId, guardiaSalidaId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Salida vehicular registrada correctamente."));
    }

    /**
     * Consulta la lista de todos los accesos vehiculares activos (en curso).
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SEGURIDAD', 'COORDINADOR_SEGURIDAD')")
    @Operation(summary = "Listar accesos activos",
               description = "Devuelve la lista de accesos vehiculares que siguen en curso. Permite roles de Seguridad, Coordinador y Administrador.")
    public ResponseEntity<ApiResponse<java.util.List<AccesoVehicularResponse>>> listarAccesosActivos() {
        java.util.List<AccesoVehicularResponse> activos = accesoVehicularService.obtenerAccesosActivos();
        return ResponseEntity.ok(ApiResponse.ok(activos));
    }

    /**
     * Consulta el historial paginado de accesos vehiculares. (HU-015)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Consultar historial de accesos",
               description = "Lista el historial paginado de accesos vehiculares con filtros opcionales. Rol requerido: ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<Page<AccesoVehicularHistorialProjection>>> consultarHistorial(
            @ModelAttribute AccesoVehicularFiltroRequest filtro,
            @PageableDefault(size = 20, sort = "horaIngreso", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AccesoVehicularHistorialProjection> historial =
                accesoVehicularService.consultarHistorial(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(historial));
    }
}
