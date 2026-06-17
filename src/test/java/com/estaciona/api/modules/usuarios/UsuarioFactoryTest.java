package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias para UsuarioFactory (POJO test).
 */
class UsuarioFactoryTest {

    private UsuarioFactory factory;
    private Rol rolUsuario;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        factory = new UsuarioFactory();

        rolUsuario = new Rol();
        rolUsuario.setId(4);
        rolUsuario.setNombre("USUARIO");
        rolUsuario.setEnabled(true);

        rolAdmin = new Rol();
        rolAdmin.setId(1);
        rolAdmin.setNombre("ADMINISTRADOR");
        rolAdmin.setEnabled(true);
    }

    @Test
    @DisplayName("debe_requerir_tipo_usuario_cuando_rol_es_usuario")
    void debe_requerir_tipo_usuario_cuando_rol_es_usuario() {
        // Arrange
        var requestSinTipo = new UsuarioRequest(
                "Juan Pérez", "juan@unicampus.edu.pe", "12345678", 4, null, "password123");

        var requestVacioTipo = new UsuarioRequest(
                "Juan Pérez", "juan@unicampus.edu.pe", "12345678", 4, "   ", "password123");

        // Act & Assert
        assertThatThrownBy(() -> factory.crear(requestSinTipo, rolUsuario, "hashedPassword"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El tipo de usuario es obligatorio para el rol USUARIO.");

        assertThatThrownBy(() -> factory.crear(requestVacioTipo, rolUsuario, "hashedPassword"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El tipo de usuario es obligatorio para el rol USUARIO.");
    }

    @Test
    @DisplayName("debe_construir_usuario_con_tipo_usuario_valido_cuando_rol_es_usuario")
    void debe_construir_usuario_con_tipo_usuario_valido_cuando_rol_es_usuario() {
        // Arrange
        var request = new UsuarioRequest(
                "Juan Pérez", "juan@unicampus.edu.pe", "12345678", 4, "ALUMNO", "password123");

        // Act
        Usuario usuario = factory.crear(request, rolUsuario, "hashedPassword");

        // Assert
        assertThat(usuario.getTipoUsuario()).isEqualTo("alumno");
        assertThat(usuario.getNombreCompleto()).isEqualTo("Juan Pérez");
        assertThat(usuario.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("debe_construir_usuario_sin_tipo_usuario_para_roles_administrativos")
    void debe_construir_usuario_sin_tipo_usuario_para_roles_administrativos() {
        // Arrange — enviamos tipoUsuario "alumno" que debe ser ignorado y seteado a null
        var request = new UsuarioRequest(
                "Admin Uno", "admin1@unicampus.edu.pe", "87654321", 1, "alumno", "password123");

        // Act
        Usuario usuario = factory.crear(request, rolAdmin, "hashedPassword");

        // Assert
        assertThat(usuario.getTipoUsuario()).isNull();
        assertThat(usuario.getNombreCompleto()).isEqualTo("Admin Uno");
        assertThat(usuario.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("debe_asignar_enabled_true_por_defecto")
    void debe_asignar_enabled_true_por_defecto() {
        // Arrange
        var request = new UsuarioRequest(
                "Admin Dos", "admin2@unicampus.edu.pe", "87654322", 1, null, "password123");

        // Act
        Usuario usuario = factory.crear(request, rolAdmin, "hashedPassword");

        // Assert
        assertThat(usuario.isEnabled()).isTrue();
    }
}
