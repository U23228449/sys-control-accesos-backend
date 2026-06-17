package com.estaciona.api.modules.auth;

import com.estaciona.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración del endpoint POST /api/v1/auth/login.
 * Usa el contenedor PostgreSQL de AbstractIntegrationTest con Flyway V1+V2 ya aplicados.
 * El usuario admin (admin@unicampus.edu.pe / Admin123!) viene de la migración V2.
 */
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String URL = "/api/v1/auth/login";

    @Test
    @DisplayName("debe_responder_200_y_token_con_credenciales_validas")
    void debe_responder_200_y_token_con_credenciales_validas() {
        // Arrange — usuario sembrado por V2__seed_data.sql
        var body = Map.of(
                "identificador", "admin@unicampus.edu.pe",
                "password", "Admin123!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(URL, body, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("token")).isNotNull().isInstanceOf(String.class);
        assertThat(data.get("tipoToken")).isEqualTo("Bearer");
        assertThat(data.get("expiraEn")).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> usuario = (Map<String, Object>) data.get("usuario");
        assertThat(usuario.get("correo")).isEqualTo("admin@unicampus.edu.pe");
        assertThat(usuario.get("rol")).isEqualTo("ADMINISTRADOR");
    }

    @Test
    @DisplayName("debe_responder_200_con_documento_como_identificador")
    void debe_responder_200_con_documento_como_identificador() {
        // Arrange — mismo usuario, usando el documento en vez del correo
        var body = Map.of(
                "identificador", "00000001",
                "password", "Admin123!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(URL, body, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("token")).isNotNull();
    }

    @Test
    @DisplayName("debe_responder_401_con_password_incorrecto")
    void debe_responder_401_con_password_incorrecto() {
        // Arrange
        var body = Map.of(
                "identificador", "admin@unicampus.edu.pe",
                "password", "PasswordMalo!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(URL, body, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("debe_responder_400_si_falta_el_campo_password")
    void debe_responder_400_si_falta_el_campo_password() {
        // Arrange — body sin el campo 'password'
        var body = Map.of("identificador", "admin@unicampus.edu.pe");

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(URL, body, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("debe_responder_401_con_usuario_inexistente")
    void debe_responder_401_con_usuario_inexistente() {
        // Arrange
        var body = Map.of(
                "identificador", "noexiste@unicampus.edu.pe",
                "password", "Admin123!"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(URL, body, Map.class);

        // Assert — misma respuesta que credenciales incorrectas (no revelar si existe)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("error")).isEqualTo("UNAUTHORIZED");
    }
}
