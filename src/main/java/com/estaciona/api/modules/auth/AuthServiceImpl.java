package com.estaciona.api.modules.auth;

import com.estaciona.api.common.exception.InvalidCredentialsException;
import com.estaciona.api.modules.auth.dto.LoginRequest;
import com.estaciona.api.modules.auth.dto.LoginResponse;
import com.estaciona.api.modules.auth.dto.LoginResponse.UsuarioResumen;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación.
 * Nunca revela si el error es "usuario no existe" vs "contraseña incorrecta" (seguridad).
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar por correo o documento (mismo valor para ambos campos)
        Usuario usuario = usuarioRepository
                .findByCorreoOrDocumento(request.identificador(), request.identificador())
                .orElseThrow(InvalidCredentialsException::new);

        // 2. Verificar que la cuenta esté activa
        if (!usuario.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        // 3. Verificar contraseña — mensaje genérico para no revelar qué falló
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 4. Generar JWT
        String token = jwtTokenProvider.generarToken(usuario);

        // 5. Construir y retornar la respuesta
        UsuarioResumen resumen = new UsuarioResumen(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getRol().getNombre()
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.obtenerExpiracionEnSegundos(),
                resumen
        );
    }
}
