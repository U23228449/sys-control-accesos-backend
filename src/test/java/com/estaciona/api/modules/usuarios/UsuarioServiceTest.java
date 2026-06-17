package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioFactory usuarioFactory;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Rol rolUsuario;
    private Rol rolAdmin;
    private UsuarioRequest requestUsuario;
    private Usuario usuarioBuilt;

    @BeforeEach
    void setUp() {
        rolUsuario = new Rol();
        rolUsuario.setId(4);
        rolUsuario.setNombre("USUARIO");
        rolUsuario.setEnabled(true);

        rolAdmin = new Rol();
        rolAdmin.setId(1);
        rolAdmin.setNombre("ADMINISTRADOR");
        rolAdmin.setEnabled(true);

        requestUsuario = new UsuarioRequest(
                "Juan Pérez", "juan@unicampus.edu.pe", "12345678", 4, "alumno", "password123");

        usuarioBuilt = Usuario.builder()
                .id(UUID.randomUUID())
                .rol(rolUsuario)
                .nombreCompleto("Juan Pérez")
                .correo("juan@unicampus.edu.pe")
                .documento("12345678")
                .tipoUsuario("alumno")
                .passwordHash("encodedPassword")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("debe_registrar_usuario_y_hashear_password_con_bcrypt")
    void debe_registrar_usuario_y_hashear_password_con_bcrypt() {
        // Arrange
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678")).thenReturn(false);
        when(rolRepository.findById(4)).thenReturn(Optional.of(rolUsuario));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioFactory.crear(requestUsuario, rolUsuario, "encodedPassword")).thenReturn(usuarioBuilt);
        when(usuarioRepository.save(usuarioBuilt)).thenReturn(usuarioBuilt);

        // Act
        UsuarioResponse response = usuarioService.registrarUsuario(requestUsuario);

        // Assert
        assertThat(response.id()).isEqualTo(usuarioBuilt.getId());
        assertThat(response.correo()).isEqualTo("juan@unicampus.edu.pe");
        assertThat(response.rol()).isEqualTo("USUARIO");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_lanzar_409_si_correo_ya_existe")
    void debe_lanzar_409_si_correo_ya_existe() {
        // Arrange
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(requestUsuario))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Usuario ya existe con correo: juan@unicampus.edu.pe");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_409_si_documento_ya_existe")
    void debe_lanzar_409_si_documento_ya_existe() {
        // Arrange
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(requestUsuario))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Usuario ya existe con documento: 12345678");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_404_si_rol_no_existe")
    void debe_lanzar_404_si_rol_no_existe() {
        // Arrange
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678")).thenReturn(false);
        when(rolRepository.findById(4)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(requestUsuario))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol no encontrado o deshabilitado con id: 4");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_404_si_rol_esta_deshabilitado")
    void debe_lanzar_404_si_rol_esta_deshabilitado() {
        // Arrange
        rolUsuario.setEnabled(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678")).thenReturn(false);
        when(rolRepository.findById(4)).thenReturn(Optional.of(rolUsuario));

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(requestUsuario))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol no encontrado o deshabilitado con id: 4");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("debe_lanzar_422_si_rol_es_usuario_y_tipo_usuario_es_nulo")
    void debe_lanzar_422_si_rol_es_usuario_y_tipo_usuario_es_nulo() {
        // Arrange
        var requestSinTipo = new UsuarioRequest(
                "Juan Pérez", "juan@unicampus.edu.pe", "12345678", 4, null, "password123");

        when(usuarioRepository.existsByCorreoIgnoreCase("juan@unicampus.edu.pe")).thenReturn(false);
        when(usuarioRepository.existsByDocumento("12345678")).thenReturn(false);
        when(rolRepository.findById(4)).thenReturn(Optional.of(rolUsuario));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioFactory.crear(any(), any(), any())).thenThrow(new BusinessRuleException("El tipo de usuario es obligatorio para el rol USUARIO."));

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.registrarUsuario(requestSinTipo))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El tipo de usuario es obligatorio para el rol USUARIO.");

        verify(usuarioRepository, never()).save(any());
    }
}
