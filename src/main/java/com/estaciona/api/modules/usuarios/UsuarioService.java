package com.estaciona.api.modules.usuarios;

import com.estaciona.api.modules.usuarios.dto.UsuarioFiltroRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResumenProjection;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateEstadoRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateRolRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioEliminadoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato para el servicio de usuarios.
 */
public interface UsuarioService {

    /** Registra un nuevo usuario en el sistema. */
    UsuarioResponse registrarUsuario(UsuarioRequest request);

    /** Permite al usuario autenticado actualizar sus propios datos personales. (HU-004) */
    UsuarioResponse actualizarMe(UUID usuarioId, UsuarioUpdateMeRequest request);

    /** Permite al ADMINISTRADOR cambiar el rol de un usuario. (HU-004) */
    UsuarioResponse actualizarRol(UUID id, UsuarioUpdateRolRequest request, UUID adminId);

    /** Permite al ADMINISTRADOR habilitar o deshabilitar un usuario. (HU-004) */
    UsuarioResponse actualizarEstado(UUID id, UsuarioUpdateEstadoRequest request, UUID adminId);

    /** Elimina (soft delete) un usuario del sistema. (HU-005) */
    UsuarioEliminadoResponseDTO eliminarUsuario(UUID id, UUID adminId);

    /** Lista y filtra usuarios con paginación. (HU-003) */
    Page<UsuarioResumenProjection> consultarUsuarios(UsuarioFiltroRequest filtro, Pageable pageable);

    /** Lista y filtra usuarios sin paginación. */
    java.util.List<UsuarioResumenProjection> consultarUsuarios(UsuarioFiltroRequest filtro);
}
