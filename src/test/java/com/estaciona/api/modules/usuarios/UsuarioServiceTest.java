package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioFiltroRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResumenProjection;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateEstadoRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateRolRequest;
import com.estaciona.api.modules.usuarios.eliminacion.UsuarioEliminacionValidationStrategy;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateCommand;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateCommandFactory;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateValidationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @Mock
    private UsuarioUpdateCommandFactory commandFactory;

    @Mock
    private UsuarioUpdateValidationStrategy updateStrategy;

    @Mock
    private UsuarioEliminacionValidationStrategy eliminacionStrategy;

    private UsuarioServiceImpl usuarioService;

    private Rol rolUsuario;
    private Rol rolAdmin;
    private UsuarioRequest requestUsuario;
    private Usuario usuarioBuilt;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                rolRepository,
                passwordEncoder,
                usuarioFactory,
                List.of(updateStrategy),
                commandFactory,
                List.of(eliminacionStrategy)
        );

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

    @Test
    @DisplayName("debe_actualizar_perfil_propio_sin_password_nuevo")
    void debe_actualizar_perfil_propio_sin_password_nuevo() {
        // Arrange
        UUID usuarioId = usuarioBuilt.getId();
        UsuarioUpdateMeRequest request = new UsuarioUpdateMeRequest(
                "Juan Pérez Modificado", "juan.mod@unicampus.edu.pe", "12345678", null, null);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioBuilt));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizarMe(usuarioId, request);

        // Assert
        assertThat(response.nombreCompleto()).isEqualTo("Juan Pérez Modificado");
        assertThat(response.correo()).isEqualTo("juan.mod@unicampus.edu.pe");
        assertThat(usuarioBuilt.getPasswordHash()).isEqualTo("encodedPassword"); // no cambió
        verify(updateStrategy, times(1)).validar(eq(usuarioBuilt), eq(request), any());
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_actualizar_perfil_propio_con_password_nuevo")
    void debe_actualizar_perfil_propio_con_password_nuevo() {
        // Arrange
        UUID usuarioId = usuarioBuilt.getId();
        UsuarioUpdateMeRequest request = new UsuarioUpdateMeRequest(
                "Juan Pérez Modificado", "juan.mod@unicampus.edu.pe", "12345678", "password123", "newPassword123");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioBuilt));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizarMe(usuarioId, request);

        // Assert
        assertThat(response.nombreCompleto()).isEqualTo("Juan Pérez Modificado");
        assertThat(usuarioBuilt.getPasswordHash()).isEqualTo("newEncodedPassword"); // cambió
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_actualizar_rol_de_usuario_como_administrador")
    void debe_actualizar_rol_de_usuario_como_administrador() {
        // Arrange
        UUID usuarioId = usuarioBuilt.getId();
        UUID adminId = UUID.randomUUID();
        UsuarioUpdateRolRequest request = new UsuarioUpdateRolRequest(1); // rol ADMINISTRADOR

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioBuilt));
        when(rolRepository.findById(1)).thenReturn(Optional.of(rolAdmin));
        
        UsuarioUpdateCommand mockCommand = mock(UsuarioUpdateCommand.class);
        when(commandFactory.crearComandoRol(usuarioBuilt, rolAdmin)).thenReturn(mockCommand);
        doAnswer(invocation -> {
            usuarioBuilt.setRol(rolAdmin);
            return null;
        }).when(mockCommand).apply(usuarioBuilt);
        
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizarRol(usuarioId, request, adminId);

        // Assert
        assertThat(response.rol()).isEqualTo("ADMINISTRADOR");
        verify(mockCommand, times(1)).apply(usuarioBuilt);
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_actualizar_estado_de_usuario_como_administrador")
    void debe_actualizar_estado_de_usuario_como_administrador() {
        // Arrange
        UUID usuarioId = usuarioBuilt.getId();
        UUID adminId = UUID.randomUUID();
        UsuarioUpdateEstadoRequest request = new UsuarioUpdateEstadoRequest(false); // desactivar

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioBuilt));
        
        UsuarioUpdateCommand mockCommand = mock(UsuarioUpdateCommand.class);
        when(commandFactory.crearComandoEstado(usuarioBuilt, false, adminId)).thenReturn(mockCommand);
        doAnswer(invocation -> {
            usuarioBuilt.setEnabled(false);
            return null;
        }).when(mockCommand).apply(usuarioBuilt);
        
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizarEstado(usuarioId, request, adminId);

        // Assert
        assertThat(response.enabled()).isFalse();
        verify(mockCommand, times(1)).apply(usuarioBuilt);
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_eliminar_usuario_soft_delete")
    void debe_eliminar_usuario_soft_delete() {
        // Arrange
        UUID usuarioId = usuarioBuilt.getId();
        UUID adminId = UUID.randomUUID();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioBuilt));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        usuarioService.eliminarUsuario(usuarioId, adminId);

        // Assert
        assertThat(usuarioBuilt.isEnabled()).isFalse();
        verify(eliminacionStrategy, times(1)).validar(usuarioBuilt, adminId, usuarioRepository);
        verify(usuarioRepository, times(1)).save(usuarioBuilt);
    }

    @Test
    @DisplayName("debe_consultar_usuarios_filtrados")
    void debe_consultar_usuarios_filtrados() {
        // Arrange
        UsuarioFiltroRequest filtro = new UsuarioFiltroRequest("USUARIO", true, "Juan");
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Usuario> page = new PageImpl<>(List.of(usuarioBuilt));
        when(usuarioRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<UsuarioResumenProjection> result = usuarioService.consultarUsuarios(filtro, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(usuarioBuilt.getId());
        assertThat(result.getContent().get(0).getNombreCompleto()).isEqualTo("Juan Pérez");
        verify(usuarioRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }
}
