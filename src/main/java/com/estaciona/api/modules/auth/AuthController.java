package com.estaciona.api.modules.auth;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.auth.dto.LoginRequest;
import com.estaciona.api.modules.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de autenticación — público, sin JWT requerido.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Operaciones de login y sesión")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Autentica un usuario y devuelve un JWT firmado HS256.
     * El campo 'identificador' acepta correo electrónico o número de documento.
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica con correo o documento + contraseña. Devuelve JWT Bearer.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Sesión iniciada correctamente."));
    }
}
