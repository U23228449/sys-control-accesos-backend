package com.estaciona.api.modules.auth;

import com.estaciona.api.common.exception.InvalidCredentialsException;
import com.estaciona.api.modules.auth.dto.LoginRequest;
import com.estaciona.api.modules.auth.dto.LoginResponse;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de autenticación.
 * Patrón: Arrange / Act / Assert.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioActivo;
    private final String PASSWORD_PLANO = "Admin123!";
    private final String PASSWORD_HASH = "$2b$12$hash";

    @BeforeEach
    void setUp() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("ADMINISTRADOR");
        rol.setEnabled(true);

        usuarioActivo = new Usuario();
        usuarioActivo.setId(UUID.randomUUID());
        usuarioActivo.setRol(rol);
        usuarioActivo.setNombreCompleto("Administrador General");
        usuarioActivo.setCorreo("admin@unicampus.edu.pe");
        usuarioActivo.setDocumento("00000001");
        usuarioActivo.setPasswordHash(PASSWORD_HASH);
        usuarioActivo.setEnabled(true);
    }

    @Test
    @DisplayName("debe_retornar_token_cuando_credenciales_son_correctas")
    void debe_retornar_token_cuando_credenciales_son_correctas() {
        // Arrange
        LoginRequest request = new LoginRequest("admin@unicampus.edu.pe", PASSWORD_PLANO);
        when(usuarioRepository.findByCorreoOrDocumento(anyString(), anyString()))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(PASSWORD_PLANO, PASSWORD_HASH)).thenReturn(true);
        when(jwtTokenProvider.generarToken(usuarioActivo)).thenReturn("jwt.token.firmado");
        when(jwtTokenProvider.obtenerExpiracionEnSegundos()).thenReturn(3600L);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("jwt.token.firmado");
        assertThat(response.tipoToken()).isEqualTo("Bearer");
        assertThat(response.expiraEn()).isEqualTo(3600L);
        assertThat(response.usuario().correo()).isEqualTo("admin@unicampus.edu.pe");
        assertThat(response.usuario().rol()).isEqualTo("ADMINISTRADOR");
        verify(jwtTokenProvider, times(1)).generarToken(usuarioActivo);
    }

    @Test
    @DisplayName("debe_lanzar_401_cuando_usuario_no_existe")
    void debe_lanzar_401_cuando_usuario_no_existe() {
        // Arrange
        LoginRequest request = new LoginRequest("noexiste@test.com", PASSWORD_PLANO);
        when(usuarioRepository.findByCorreoOrDocumento(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        // No debe intentar verificar la contraseña si el usuario no existe
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generarToken(any());
    }

    @Test
    @DisplayName("debe_lanzar_401_cuando_password_no_coincide")
    void debe_lanzar_401_cuando_password_no_coincide() {
        // Arrange
        LoginRequest request = new LoginRequest("admin@unicampus.edu.pe", "PasswordIncorrecto!");
        when(usuarioRepository.findByCorreoOrDocumento(anyString(), anyString()))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("PasswordIncorrecto!", PASSWORD_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        // El mensaje debe ser genérico para no revelar información
        assertThatThrownBy(() -> authService.login(request))
                .hasMessage("Credenciales inválidas. Verifica tu identificador y contraseña.");

        verify(jwtTokenProvider, never()).generarToken(any());
    }

    @Test
    @DisplayName("debe_lanzar_401_cuando_usuario_esta_deshabilitado")
    void debe_lanzar_401_cuando_usuario_esta_deshabilitado() {
        // Arrange
        usuarioActivo.setEnabled(false);
        LoginRequest request = new LoginRequest("admin@unicampus.edu.pe", PASSWORD_PLANO);
        when(usuarioRepository.findByCorreoOrDocumento(anyString(), anyString()))
                .thenReturn(Optional.of(usuarioActivo));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        // No debe verificar la contraseña de una cuenta deshabilitada
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generarToken(any());
    }
}
