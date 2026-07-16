package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.modules.configuraciones.dto.ConfiguracionResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionUpdateRequest;
import com.estaciona.api.modules.configuraciones.entity.Configuracion;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfiguracionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String URL = "/api/v1/configuraciones";
    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeAll
    void setupConfiguracionesPrueba() {
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR").orElseThrow();
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();

        Usuario admin = Usuario.builder()
                .rol(rolAdmin)
                .nombreCompleto("Admin Configs")
                .correo("admin.configs@unicampus.edu.pe")
                .documento("ADMCFG1")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(admin);

        Usuario user = Usuario.builder()
                .rol(rolUsuario)
                .nombreCompleto("User Configs")
                .correo("user.configs@unicampus.edu.pe")
                .documento("USRCFG1")
                .passwordHash(passwordEncoder.encode("User123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(user);

        tokenAdmin = "Bearer " + loginYObtenerToken("admin.configs@unicampus.edu.pe", "Admin123!");
        tokenUsuario = "Bearer " + loginYObtenerToken("user.configs@unicampus.edu.pe", "User123!");
    }

    @Test
    @DisplayName("debe_listar_configuraciones_para_admin")
    void debe_listar_configuraciones_para_admin() {
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(tokenAdmin, null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        java.util.List<?> data = (java.util.List<?>) body.get("data");
        assertThat(data).isNotEmpty();
    }

    @Test
    @DisplayName("debe_responder_403_si_no_es_admin_al_listar")
    void debe_responder_403_si_no_es_admin_al_listar() {
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(tokenUsuario, null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_actualizar_configuracion_valida")
    void debe_actualizar_configuracion_valida() {
        ConfiguracionUpdateRequest request = new ConfiguracionUpdateRequest("10");

        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/MAX_INTENTOS_LOGIN", HttpMethod.PATCH, crearRequest(tokenAdmin, request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertThat(data.get("valor")).isEqualTo("10");

        // Verificar en base de datos
        Configuracion configDb = configuracionRepository.findByClaveAndEnabledTrue("MAX_INTENTOS_LOGIN").orElseThrow();
        assertThat(configDb.getValor()).isEqualTo("10");
    }

    @Test
    @DisplayName("debe_lanzar_422_si_valor_invalido")
    void debe_lanzar_422_si_valor_invalido() {
        ConfiguracionUpdateRequest request = new ConfiguracionUpdateRequest("invalido");

        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/MAX_INTENTOS_LOGIN", HttpMethod.PATCH, crearRequest(tokenAdmin, request), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private <T> HttpEntity<T> crearRequest(String bearerToken, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
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
