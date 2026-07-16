package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.dto.ApiResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioFiltroRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResumenProjection;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateEstadoRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateRolRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioEliminadoResponseDTO;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Endpoints para la gestión de usuarios.
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
     * Registra un nuevo usuario en el sistema. (HU-001)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar nuevo usuario",
               description = "Permite registrar un nuevo usuario con un rol específico. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrarUsuario(
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.registrarUsuario(request);
        URI location = URI.create("/api/v1/usuarios/" + response.id());
        return ResponseEntity
                .created(location)
                .body(ApiResponse.ok(response, "Usuario registrado correctamente."));
    }

    /**
     * Lista y filtra usuarios con paginación o lista completa. (HU-003)
     * IMPORTANTE: este endpoint GET estático se mapea ANTES que /{id}/rol y /{id}/estado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SEGURIDAD', 'COORDINADOR_SEGURIDAD')")
    @Operation(summary = "Consultar usuarios",
               description = "Lista usuarios con filtros opcionales (rol, estado, búsqueda por texto).")
    public ResponseEntity<ApiResponse<?>> consultarUsuarios(
            @ModelAttribute UsuarioFiltroRequest filtro,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            jakarta.servlet.http.HttpServletRequest request) {
        if (request.getParameter("page") == null && request.getParameter("size") == null) {
            java.util.List<UsuarioResumenProjection> usuarios = usuarioService.consultarUsuarios(filtro);
            return ResponseEntity.ok(ApiResponse.ok(usuarios));
        } else {
            Page<UsuarioResumenProjection> usuarios = usuarioService.consultarUsuarios(filtro, pageable);
            return ResponseEntity.ok(ApiResponse.ok(usuarios));
        }
    }

    /**
     * Permite al usuario autenticado (rol USUARIO) actualizar sus propios datos personales. (HU-004)
     * NOTA: /me se declara antes que /{id} para evitar ambigüedad.
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    @Operation(summary = "Actualizar perfil propio",
               description = "Permite al usuario autenticado actualizar su nombre, correo, documento y contraseña.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarMe(
            @Valid @RequestBody UsuarioUpdateMeRequest request) {
        UUID usuarioId = SecurityContextUtils.obtenerUsuarioIdActual();
        UsuarioResponse response = usuarioService.actualizarMe(usuarioId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Perfil actualizado correctamente."));
    }

    /**
     * Permite al ADMINISTRADOR cambiar el rol de cualquier usuario. (HU-004)
     */
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar rol de usuario",
               description = "Permite al ADMINISTRADOR asignar un nuevo rol a un usuario. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarRol(
            @PathVariable UUID id,
            @Valid @RequestBody UsuarioUpdateRolRequest request) {
        UUID adminId = SecurityContextUtils.obtenerUsuarioIdActual();
        UsuarioResponse response = usuarioService.actualizarRol(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Rol actualizado correctamente."));
    }

    /**
     * Permite al ADMINISTRADOR habilitar o deshabilitar un usuario. (HU-004)
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado de usuario",
               description = "Permite al ADMINISTRADOR habilitar o deshabilitar un usuario. Protege al último admin activo.")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarEstado(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid UsuarioUpdateEstadoRequest request) {
        UUID adminId = SecurityContextUtils.obtenerUsuarioIdActual();
        UsuarioResponse response = usuarioService.actualizarEstado(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Estado actualizado correctamente."));
    }

    /**
     * Elimina un usuario (soft delete). (HU-005)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar usuario",
               description = "Deshabilita un usuario (soft delete). No se puede eliminar al último administrador activo.")
    public ResponseEntity<ApiResponse<UsuarioEliminadoResponseDTO>> eliminarUsuario(@PathVariable UUID id) {
        UUID adminId = SecurityContextUtils.obtenerUsuarioIdActual();
        UsuarioEliminadoResponseDTO response = usuarioService.eliminarUsuario(id, adminId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Usuario eliminado correctamente."));
    }
}
