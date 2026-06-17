package com.estaciona.api.modules.usuarios;

import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración del endpoint POST /api/v1/usuarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsuarioControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String URL = "/api/v1/usuarios";
    private String tokenAdmin;
    private String tokenSeguridad;

    @BeforeAll
    void setupUsuariosPrueba() {
        // Obtener token del admin semilla
        tokenAdmin = "Bearer " + loginYObtenerToken("admin@unicampus.edu.pe", "Admin123!");

        // Crear un usuario de rol SEGURIDAD para probar el 403 Forbidden
        Rol rolSeguridad = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
        Usuario seguridad = Usuario.builder()
                .rol(rolSeguridad)
                .nombreCompleto("Guardia Test")
                .correo("seguridad@unicampus.edu.pe")
                .documento("00000002")
                .passwordHash(passwordEncoder.encode("Seguridad123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(seguridad);

        tokenSeguridad = "Bearer " + loginYObtenerToken("seguridad@unicampus.edu.pe", "Seguridad123!");
    }

    @Test
    @DisplayName("debe_responder_201_al_crear_usuario_valido_con_rol_usuario_y_tipo_alumno")
    void debe_responder_201_al_crear_usuario_valido_con_rol_usuario_y_tipo_alumno() {
        // Arrange
        String correo = "alumno" + System.currentTimeMillis() + "@unicampus.edu.pe";
        String documento = "D" + (System.currentTimeMillis() % 10000000);
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();

        var body = Map.of(
                "nombreCompleto", "Alumno Nuevo",
                "correo", correo,
                "documento", documento,
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "alumno",
                "password", "Password123!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert — 201 Created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isNotNull();
        assertThat(data.get("nombreCompleto")).isEqualTo("Alumno Nuevo");
        assertThat(data.get("correo")).isEqualTo(correo);
        assertThat(data.get("rol")).isEqualTo("USUARIO");
        assertThat(data.get("tipoUsuario")).isEqualTo("alumno");
        assertThat((Boolean) data.get("enabled")).isTrue();

        // Verificar en BD que el password hash es BCrypt y coincide
        UUID id = UUID.fromString((String) data.get("id"));
        Usuario guardado = usuarioRepository.findById(id).orElseThrow();
        assertThat(guardado.getPasswordHash()).startsWith("$2a$12$"); // BCrypt strength 12
        assertThat(passwordEncoder.matches("Password123!", guardado.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("debe_responder_403_si_quien_crea_no_es_administrador")
    void debe_responder_403_si_quien_crea_no_es_administrador() {
        // Arrange
        var body = Map.of(
                "nombreCompleto", "Otro Usuario",
                "correo", "otro@unicampus.edu.pe",
                "documento", "00009999",
                "rolId", 4,
                "tipoUsuario", "alumno",
                "password", "Password123!"
        );

        // Act — intentar crear usando token de SEGURIDAD
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("error")).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("debe_responder_409_si_correo_duplicado")
    void debe_responder_409_si_correo_duplicado() {
        // Arrange — crear un usuario primero
        String correo = "duplicado@unicampus.edu.pe";
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        var body1 = Map.of(
                "nombreCompleto", "Usuario Uno",
                "correo", correo,
                "documento", "D0000001",
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "docente",
                "password", "Password123!"
        );
        restTemplate.exchange(URL, HttpMethod.POST, crearRequest(body1, tokenAdmin), Map.class);

        // Act — intentar registrar la misma cuenta con mismo correo (e incluso diferente case)
        var body2 = Map.of(
                "nombreCompleto", "Usuario Dos",
                "correo", correo.toUpperCase(),
                "documento", "D0000002",
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "docente",
                "password", "Password123!"
        );
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body2, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("debe_responder_409_si_documento_duplicado")
    void debe_responder_409_si_documento_duplicado() {
        // Arrange
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        var body1 = Map.of(
                "nombreCompleto", "Usuario Tres",
                "correo", "tres@unicampus.edu.pe",
                "documento", "DOC999",
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "docente",
                "password", "Password123!"
        );
        restTemplate.exchange(URL, HttpMethod.POST, crearRequest(body1, tokenAdmin), Map.class);

        // Act
        var body2 = Map.of(
                "nombreCompleto", "Usuario Cuatro",
                "correo", "cuatro@unicampus.edu.pe",
                "documento", "DOC999", // documento duplicado
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "docente",
                "password", "Password123!"
        );
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body2, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("debe_responder_422_si_rol_usuario_sin_tipo_usuario")
    void debe_responder_422_si_rol_usuario_sin_tipo_usuario() {
        // Arrange — rol USUARIO pero sin tipoUsuario
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        var body = Map.of(
                "nombreCompleto", "Usuario Fallido",
                "correo", "fallido@unicampus.edu.pe",
                "documento", "DOCFAL",
                "rolId", rolUsuario.getId(),
                // tipoUsuario omitido
                "password", "Password123!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("debe_responder_400_si_password_menor_a_8_caracteres")
    void debe_responder_400_si_password_menor_a_8_caracteres() {
        // Arrange
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        var body = Map.of(
                "nombreCompleto", "Usuario Corto",
                "correo", "corto@unicampus.edu.pe",
                "documento", "DOCCOR",
                "rolId", rolUsuario.getId(),
                "tipoUsuario", "alumno",
                "password", "short" // < 8 chars
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    private HttpEntity<Map> crearRequest(Map body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);
        return new HttpEntity<>(body, headers);
    }

    @SuppressWarnings("unchecked")
    private String loginYObtenerToken(String identificador, String password) {
        var body = Map.of("identificador", identificador, "password", password);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }
}
