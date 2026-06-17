package com.estaciona.api.modules.zonas;

import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración del endpoint POST /api/v1/zonas-estacionamiento.
 * Usa el contenedor PostgreSQL de AbstractIntegrationTest con Flyway V1+V2 aplicados.
 *
 * Estrategia de tokens:
 *  - Admin: usuario sembrado en V2 (admin@unicampus.edu.pe / Admin123!)
 *  - SEGURIDAD: usuario creado programáticamente en @BeforeAll
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZonaControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String URL = "/api/v1/zonas-estacionamiento";
    private String tokenAdmin;
    private String tokenSeguridad;

    @BeforeAll
    void configurarDatosYTokens() {
        // Token del admin (usuario de V2 seed)
        tokenAdmin = "Bearer " + loginYObtenerToken("admin@unicampus.edu.pe", "Admin123!");

        // Crear usuario SEGURIDAD para el test 403 (si aún no existe)
        boolean existe = usuarioRepository.existsByCorreo("guardia@test.com");
        if (!existe) {
            var rolSeguridad = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
            Usuario guardia = new Usuario();
            guardia.setRol(rolSeguridad);
            guardia.setNombreCompleto("Guardia Test");
            guardia.setCorreo("guardia@test.com");
            guardia.setDocumento("88888888");
            guardia.setPasswordHash(passwordEncoder.encode("Guardia123!"));
            guardia.setEnabled(true);
            usuarioRepository.save(guardia);
        }
        tokenSeguridad = "Bearer " + loginYObtenerToken("guardia@test.com", "Guardia123!");
    }

    @Test
    @DisplayName("debe_responder_201_al_crear_zona_valida")
    void debe_responder_201_al_crear_zona_valida() {
        // Arrange — campus_id=1 (Campus Central) viene del seed V2
        var body = Map.of(
                "campusId", 1,
                "nombre", "Zona Norte IT-" + System.currentTimeMillis(),
                "ubicacion", "Edificio A, planta baja",
                "tipo", "cubierta",
                "aforoMaximo", 30
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isNotNull();
        assertThat(data.get("aforoDisponible")).isEqualTo(30);
        assertThat(data.get("aforoMaximo")).isEqualTo(30);
        assertThat(data.get("estado")).isEqualTo("activa");
        assertThat(data.get("campusNombre")).isEqualTo("Campus Central");
        // Verificar que createdAt fue asignado automáticamente
        assertThat(data.get("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("debe_responder_403_si_el_usuario_no_es_administrador")
    void debe_responder_403_si_el_usuario_no_es_administrador() {
        // Arrange — SEGURIDAD no puede crear zonas
        var body = Map.of(
                "campusId", 1,
                "nombre", "Zona Prohibida",
                "tipo", "abierta",
                "aforoMaximo", 10
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_409_al_duplicar_nombre_de_zona_en_mismo_campus")
    void debe_responder_409_al_duplicar_nombre_de_zona_en_mismo_campus() {
        // Arrange — crear la primera zona
        String nombreZona = "Zona Duplicada-" + System.currentTimeMillis();
        var body = Map.of(
                "campusId", 1,
                "nombre", nombreZona,
                "tipo", "cubierta",
                "aforoMaximo", 20
        );
        restTemplate.exchange(URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Act — intentar crear la misma zona
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("debe_responder_404_si_campus_id_no_existe")
    void debe_responder_404_si_campus_id_no_existe() {
        // Arrange — campus 9999 no existe
        var body = Map.of(
                "campusId", 9999,
                "nombre", "Zona Fantasma",
                "tipo", "abierta",
                "aforoMaximo", 10
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("debe_responder_400_si_aforo_maximo_es_negativo_o_nulo")
    void debe_responder_400_si_aforo_maximo_es_negativo_o_nulo() {
        // Arrange — aforoMaximo = -5 viola @Positive
        var body = Map.of(
                "campusId", 1,
                "nombre", "Zona Invalida",
                "tipo", "abierta",
                "aforoMaximo", -5
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    /** Construye un HttpEntity con body JSON y header Authorization. */
    @SuppressWarnings("unchecked")
    private HttpEntity<Map> crearRequest(Map body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);
        return new HttpEntity<>(body, headers);
    }

    /** Llama al login y retorna solo el token (sin "Bearer "). */
    @SuppressWarnings("unchecked")
    private String loginYObtenerToken(String identificador, String password) {
        var body = Map.of("identificador", identificador, "password", password);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }
}
