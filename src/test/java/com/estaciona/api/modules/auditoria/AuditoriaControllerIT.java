package com.estaciona.api.modules.auditoria;

import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.VehiculoRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditoriaControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL = "/api/v1/auditoria/eventos";

    private String tokenAdmin;
    private String tokenCoordinador;
    private String tokenUsuario;

    private Usuario propietario;

    @BeforeAll
    void setupUsuariosPrueba() {
        tokenAdmin = "Bearer " + loginYObtenerToken("admin@unicampus.edu.pe", "Admin123!");

        // Crear y guardar un COORDINADOR_SEGURIDAD
        Rol rolCoordinador = rolRepository.findByNombre("COORDINADOR_SEGURIDAD").orElseThrow();
        Usuario coordinador = Usuario.builder()
                .rol(rolCoordinador)
                .nombreCompleto("Coordinador Seguridad")
                .correo("coordinador.seguridad@unicampus.edu.pe")
                .documento("COORD01")
                .passwordHash(passwordEncoder.encode("Coord123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(coordinador);
        tokenCoordinador = "Bearer " + loginYObtenerToken("coordinador.seguridad@unicampus.edu.pe", "Coord123!");

        // Crear y guardar un USUARIO común
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        propietario = Usuario.builder()
                .rol(rolUsuario)
                .nombreCompleto("Usuario Comun")
                .correo("usuario.auditoria@unicampus.edu.pe")
                .documento("USR009")
                .passwordHash(passwordEncoder.encode("Usuario123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(propietario);
        tokenUsuario = "Bearer " + loginYObtenerToken("usuario.auditoria@unicampus.edu.pe", "Usuario123!");
    }

    @Test
    @DisplayName("debe_generar_log_de_auditoria_automaticamente_al_insertar_y_actualizar_un_vehiculo")
    void debe_generar_log_de_auditoria_automaticamente_al_insertar_y_actualizar_un_vehiculo() throws Exception {
        // --- 1. INSERT ---
        String placaUnica = "AUD" + (System.currentTimeMillis() % 100000);
        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placaUnica)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Mazda 3")
                .color("Azul")
                .enabled(true)
                .build();

        // Guardar vehículo (gatilla trigger AFTER INSERT)
        Vehiculo guardado = vehiculoRepository.saveAndFlush(vehiculo);

        // Buscar log de auditoría
        LogAuditoria logInsert = logAuditoriaRepository.findAll().stream()
                .filter(log -> "vehiculos".equals(log.getTablaAfectada())
                        && guardado.getId().toString().equals(log.getRegistroId())
                        && "INSERT".equals(log.getAccion()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se generó el log de INSERT."));

        assertThat(logInsert.getValoresAnteriores()).isNull();
        assertThat(logInsert.getValoresNuevos()).contains(placaUnica);

        // --- 2. UPDATE ---
        // Modificar vehículo (gatilla trigger AFTER UPDATE)
        guardado.setColor("Verde");
        Vehiculo actualizado = vehiculoRepository.saveAndFlush(guardado);

        // Buscar log de UPDATE
        LogAuditoria logUpdate = logAuditoriaRepository.findAll().stream()
                .filter(log -> "vehiculos".equals(log.getTablaAfectada())
                        && actualizado.getId().toString().equals(log.getRegistroId())
                        && "UPDATE".equals(log.getAccion()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se generó el log de UPDATE."));

        assertThat(logUpdate.getValoresAnteriores()).contains("Azul");
        assertThat(logUpdate.getValoresNuevos()).contains("Verde");
    }

    @Test
    @DisplayName("debe_lanzar_error_al_intentar_eliminar_o_actualizar_un_log_de_auditoria")
    void debe_lanzar_error_al_intentar_eliminar_o_actualizar_un_log_de_auditoria() {
        // Arrange — Insertar un log directamente
        LogAuditoria log = LogAuditoria.builder()
                .tablaAfectada("pruebas")
                .registroId(UUID.randomUUID().toString())
                .accion("INSERT")
                .valoresNuevos("{\"test\": true}")
                .fecha(java.time.OffsetDateTime.now())
                .build();

        // Guardar log inicial sin gatillar excepción (ya que es INSERT)
        LogAuditoria guardado = logAuditoriaRepository.saveAndFlush(log);

        // Act & Assert — Usar una transacción para probar que el trigger de BD bloquea la operación
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        // 1. Probar UPDATE (debe fallar)
        assertThatThrownBy(() -> tx.execute(status -> {
            guardado.setTablaAfectada("cambio_prohibido");
            logAuditoriaRepository.saveAndFlush(guardado);
            return null;
        })).isInstanceOf(Exception.class); // Spring envuelve la excepción de SQL

        // 2. Probar DELETE (debe fallar)
        assertThatThrownBy(() -> tx.execute(status -> {
            logAuditoriaRepository.delete(guardado);
            logAuditoriaRepository.flush();
            return null;
        })).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("debe_responder_201_al_registrar_evento_manual_como_administrador")
    void debe_responder_201_al_registrar_evento_manual_como_administrador() {
        // Arrange
        var body = Map.of(
                "tablaAfectada", "configuraciones",
                "registroId", "12",
                "accion", "INSERT",
                "valoresNuevos", "{\"clave\":\"MAX_INTENTOS_LOGIN\",\"valor\":\"6\"}"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isNotNull();
        assertThat(data.get("tablaAfectada")).isEqualTo("configuraciones");
        assertThat(data.get("accion")).isEqualTo("INSERT");
        assertThat(data.get("usuario")).isEqualTo("Administrador General");
    }

    @Test
    @DisplayName("debe_responder_403_al_registrar_evento_manual_sin_rol_administrador")
    void debe_responder_403_al_registrar_evento_manual_sin_rol_administrador() {
        // Arrange
        var body = Map.of(
                "tablaAfectada", "configuraciones",
                "registroId", "12",
                "accion", "INSERT",
                "valoresNuevos", "{\"test\":true}"
        );

        // Act — intentar con Coordinador de Seguridad
        ResponseEntity<Map> responseCoord = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenCoordinador), Map.class);

        // Assert
        assertThat(responseCoord.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_200_y_paginar_correctamente_get_eventos")
    void debe_responder_200_y_paginar_correctamente_get_eventos() {
        // Act — Coordinador de Seguridad sí tiene permiso de lectura
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "?page=0&size=5", HttpMethod.GET, crearRequest(null, tokenCoordinador), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("content")).isNotNull();
    }

    @Test
    @DisplayName("debe_filtrar_por_tabla_afectada_y_rango_de_fechas")
    void debe_filtrar_por_tabla_afectada_y_rango_de_fechas() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "?tablaAfectada=vehiculos", HttpMethod.GET, crearRequest(null, tokenCoordinador), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("debe_responder_404_al_consultar_detalle_de_evento_inexistente")
    void debe_responder_404_al_consultar_detalle_de_evento_inexistente() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + UUID.randomUUID(), HttpMethod.GET, crearRequest(null, tokenCoordinador), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
