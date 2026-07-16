package com.estaciona.api.modules.usuarios.eliminacion;

import com.estaciona.api.common.exception.ForbiddenOperationException;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Estrategia que impide que un administrador se elimine a sí mismo.
 */
@Component
public class NoAutoEliminacionStrategy implements UsuarioEliminacionValidationStrategy {

    @Override
    public void validar(Usuario usuario, UUID adminId, UsuarioRepository repo) {
        if (usuario.getId().equals(adminId)) {
            throw new ForbiddenOperationException("Un administrador no puede eliminarse a sí mismo.");
        }
    }
}
