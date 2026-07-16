package com.estaciona.api.modules.usuarios.update;

import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Estrategia de validación para actualización de perfil propio del usuario.
 * Cada implementación valida un aspecto diferente del request antes de persistir.
 */
public interface UsuarioUpdateValidationStrategy {

    /**
     * Ejecuta la validación sobre el usuario actual y los datos del request.
     *
     * @param usuarioActual entidad existente en base de datos.
     * @param request       datos nuevos enviados por el cliente.
     * @param encoder       codificador BCrypt para verificar contraseñas.
     * @throws com.estaciona.api.common.exception.DuplicateResourceException si hay duplicados.
     * @throws com.estaciona.api.common.exception.BusinessRuleException       si se viola una regla de negocio.
     */
    void validar(Usuario usuarioActual, UsuarioUpdateMeRequest request, PasswordEncoder encoder);
}
