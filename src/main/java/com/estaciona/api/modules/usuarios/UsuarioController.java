package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
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
 * Endpoints para la gestión de usuarios.
 * Acceso exclusivo para el rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Permite registrar un nuevo usuario con un rol específico. Si el rol es 'USUARIO', " +
                          "el campo 'tipoUsuario' es obligatorio. Requiere rol ADMINISTRADOR."
    )
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarUsuario(
            @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse response = usuarioService.registrarUsuario(request);
        URI location = URI.create("/api/v1/usuarios/" + response.id());

        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(response, "Usuario registrado correctamente."));
    }
}
