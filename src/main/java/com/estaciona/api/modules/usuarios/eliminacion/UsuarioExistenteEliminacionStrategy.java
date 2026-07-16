package com.estaciona.api.modules.usuarios.eliminacion;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Estrategia que valida que el usuario esté habilitado antes de eliminarlo.
 * La verificación de existencia (404) se realiza en el service antes de invocar las estrategias.
 */
@Component
public class UsuarioExistenteEliminacionStrategy implements UsuarioEliminacionValidationStrategy {

    @Override
    public void validar(Usuario usuario, UUID adminId, UsuarioRepository repo) {
        if (!usuario.isEnabled()) {
            throw new BusinessRuleException("El usuario ya está deshabilitado.");
        }
    }
}
