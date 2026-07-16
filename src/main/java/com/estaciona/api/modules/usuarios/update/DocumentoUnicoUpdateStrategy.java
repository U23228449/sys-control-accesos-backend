package com.estaciona.api.modules.usuarios.update;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Estrategia que valida que el nuevo documento no esté en uso por otro usuario distinto.
 */
@Component
public class DocumentoUnicoUpdateStrategy implements UsuarioUpdateValidationStrategy {

    private final UsuarioRepository usuarioRepository;

    public DocumentoUnicoUpdateStrategy(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void validar(Usuario usuarioActual, UsuarioUpdateMeRequest request, PasswordEncoder encoder) {
        if (usuarioRepository.existsByDocumentoAndIdNot(request.documento(), usuarioActual.getId())) {
            throw new DuplicateResourceException("Usuario", "documento", request.documento());
        }
    }
}
