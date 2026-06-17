package com.estaciona.api.security;

import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Carga el usuario desde la BD por UUID (desde el filtro JWT) o por correo/documento (desde login).
 * Spring Security llama a este servicio al autenticar.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOpt = intentarPorUuid(identificador);

        // Si no es UUID válido, busca por correo o documento (flujo de login)
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByCorreoOrDocumento(identificador, identificador);
        }

        Usuario usuario = usuarioOpt.orElseThrow(() ->
                new UsernameNotFoundException("Usuario no encontrado: " + identificador));

        return new CustomUserDetails(usuario);
    }

    /** Intenta parsear el identificador como UUID y buscarlo en la BD. */
    private Optional<Usuario> intentarPorUuid(String identificador) {
        try {
            return usuarioRepository.findById(UUID.fromString(identificador));
        } catch (IllegalArgumentException e) {
            // No es un UUID válido, flujo normal de correo/documento
            return Optional.empty();
        }
    }
}
