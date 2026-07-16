package com.estaciona.api.modules.usuarios.update;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Estrategia que valida que la contraseña actual del usuario sea correcta
 * cuando se solicita un cambio de contraseña.
 */
@Component
public class PasswordActualCorrectoStrategy implements UsuarioUpdateValidationStrategy {

    @Override
    public void validar(Usuario usuarioActual, UsuarioUpdateMeRequest request, PasswordEncoder encoder) {
        // Solo valida si se quiere cambiar la contraseña
        if (request.passwordNuevo() != null) {
            if (request.passwordActual() == null) {
                throw new BusinessRuleException("Se debe proporcionar la contraseña actual para cambiar la contraseña.");
            }
            if (!encoder.matches(request.passwordActual(), usuarioActual.getPasswordHash())) {
                throw new BusinessRuleException("Contraseña actual incorrecta.");
            }
        }
    }
}
