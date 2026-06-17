package com.estaciona.api.modules.usuarios;

import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;

/**
 * Contrato para el servicio de usuarios.
 */
public interface UsuarioService {

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request datos del nuevo usuario.
     * @return DTO con la información del usuario registrado.
     */
    UsuarioResponse registrarUsuario(UsuarioRequest request);
}
