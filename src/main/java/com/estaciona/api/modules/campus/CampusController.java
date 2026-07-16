package com.estaciona.api.modules.campus;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.campus.entity.Campus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para campus.
 */
@RestController
@RequestMapping("/api/v1/campus")
@Tag(name = "Campus", description = "Endpoints de consulta de campus")
@SecurityRequirement(name = "bearerAuth")
public class CampusController {

    private final CampusRepository campusRepository;

    public CampusController(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    /**
     * Obtiene todos los campus registrados.
     */
    @GetMapping
    @Operation(summary = "Listar campus", description = "Devuelve la lista completa de campus registrados.")
    public ResponseEntity<ApiResponse<List<Campus>>> listarCampus() {
        List<Campus> campusList = campusRepository.findAll();
        return ResponseEntity.ok(ApiResponse.ok(campusList, "Campus obtenidos correctamente."));
    }
}
