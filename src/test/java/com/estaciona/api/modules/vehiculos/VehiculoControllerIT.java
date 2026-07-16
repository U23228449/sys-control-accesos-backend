package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.support.AbstractIntegrationTest;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.entity.Zona;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de integración del endpoint POST /api/v1/vehiculos.
 * El usuario admin (admin@unicampus.edu.pe / Admin123!) viene del seed V2.
 * El propietario se obtiene del token JWT — nunca del body.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VehiculoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private com.estaciona.api.modules.usuarios.UsuarioRepository usuarioRepository;

    @Autowired
    private com.estaciona.api.modules.roles.RolRepository rolRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.estaciona.api.modules.accesos.AccesoVehicularRepository accesoVehicularRepository;

    @Autowired
    private com.estaciona.api.modules.zonas.ZonaRepository zonaRepository;

    @Autowired
    private com.estaciona.api.modules.campus.CampusRepository campusRepository;

    private static final String URL = "/api/v1/vehiculos";
    private String tokenAdmin;
    private String tokenSeguridad;
    private String tokenUsuario;
    private com.estaciona.api.modules.usuarios.entity.Usuario propietario;

    @BeforeAll
    void configurarToken() {
        tokenAdmin = "Bearer " + loginYObtenerToken("admin@unicampus.edu.pe", "Admin123!");

        // Crear usuario con rol SEGURIDAD
        com.estaciona.api.modules.roles.entity.Rol rolSeg = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
        com.estaciona.api.modules.usuarios.entity.Usuario seg = com.estaciona.api.modules.usuarios.entity.Usuario.builder()
                .rol(rolSeg)
                .nombreCompleto("Guardia Vehiculos")
                .correo("guardia.vehiculos@unicampus.edu.pe")
                .documento("SEC0099")
                .passwordHash(passwordEncoder.encode("Guardia123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(seg);
        tokenSeguridad = "Bearer " + loginYObtenerToken("guardia.vehiculos@unicampus.edu.pe", "Guardia123!");

        // Crear usuario con rol USUARIO
        com.estaciona.api.modules.roles.entity.Rol rolUsr = rolRepository.findByNombre("USUARIO").orElseThrow();
        propietario = com.estaciona.api.modules.usuarios.entity.Usuario.builder()
                .rol(rolUsr)
                .nombreCompleto("Propietario Vehiculos")
                .correo("propietario.vehiculos@unicampus.edu.pe")
                .documento("USR0099")
                .passwordHash(passwordEncoder.encode("Usuario123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(propietario);
        tokenUsuario = "Bearer " + loginYObtenerToken("propietario.vehiculos@unicampus.edu.pe", "Usuario123!");
    }

    @Test
    @DisplayName("debe_responder_201_y_asociar_vehiculo_al_usuario_del_token")
    void debe_responder_201_y_asociar_vehiculo_al_usuario_del_token() {
        // Arrange — placa única con timestamp
        String placa = "TST" + (System.currentTimeMillis() % 100000);
        var body = Map.of(
                "tipo", "auto",
                "placa", placa,
                "marcaModelo", "Toyota Corolla 2023",
                "color", "Blanco"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert — 201 con datos correctos
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isNotNull();
        assertThat(data.get("tipo")).isEqualTo("auto");
        assertThat(data.get("placa")).isEqualTo(placa.toUpperCase().replace(" ", ""));

        @SuppressWarnings("unchecked")
        Map<String, Object> propietario = (Map<String, Object>) data.get("propietario");
        assertThat(propietario.get("nombreCompleto")).isEqualTo("Administrador General");

        // Verificar en BD que el usuario_id corresponde al admin del token (via findByUsuarioId)
        String vehiculoId   = (String) data.get("id");
        String propietarioId = (String) propietario.get("id");
        var vehiculosDelAdmin = vehiculoRepository.findByUsuarioId(
                java.util.UUID.fromString(propietarioId));
        assertThat(vehiculosDelAdmin)
                .anyMatch(v -> v.getId().toString().equals(vehiculoId));
    }

    @Test
    @DisplayName("debe_responder_409_si_la_placa_ya_esta_registrada")
    void debe_responder_409_si_la_placa_ya_esta_registrada() {
        // Arrange — registrar la primera vez
        String placa = "DUP" + (System.currentTimeMillis() % 100000);
        var body = Map.of(
                "tipo", "auto", "placa", placa,
                "marcaModelo", "Toyota", "color", "Rojo"
        );
        restTemplate.exchange(URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Act — intentar registrar la misma placa
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("debe_responder_400_si_la_placa_tiene_formato_invalido")
    void debe_responder_400_si_la_placa_tiene_formato_invalido() {
        // Arrange — placa muy corta (viola @Pattern mínimo 6 chars)
        var body = Map.of(
                "tipo", "auto", "placa", "AB",
                "marcaModelo", "Toyota", "color", "Azul"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("debe_responder_400_si_tipo_no_es_auto_ni_moto")
    void debe_responder_400_si_tipo_no_es_auto_ni_moto() {
        // Arrange — tipo inválido
        var body = Map.of(
                "tipo", "camion", "placa", "ABC123",
                "marcaModelo", "Ford", "color", "Gris"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("debe_responder_401_si_no_se_envia_token")
    void debe_responder_401_si_no_se_envia_token() {
        // Arrange — sin Authorization header
        var body = Map.of(
                "tipo", "auto", "placa", "SIN999",
                "marcaModelo", "Toyota", "color", "Blanco"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("debe_ignorar_usuarioId_en_body_y_usar_el_del_token")
    void debe_ignorar_usuarioId_en_body_y_usar_el_del_token() {
        // Arrange — body incluye un campo extra 'usuarioId' (que el DTO no tiene)
        // El DTO NO tiene ese campo, así que Jackson lo ignora y usa el del JWT
        String placa = "IGN" + (System.currentTimeMillis() % 100000);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("tipo", "moto");
        body.put("placa", placa);
        body.put("marcaModelo", "Honda PCX");
        body.put("color", "Azul");
        body.put("usuarioId", "00000000-0000-0000-0000-000000000000"); // campo fantasma

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert — 201 y el propietario es el admin del token, no el UUID falso
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> propietario = (Map<String, Object>) data.get("propietario");
        assertThat(propietario.get("nombreCompleto")).isEqualTo("Administrador General");
    }

    private HttpEntity<Map> crearRequest(Map body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> crearRequest(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        return new HttpEntity<>(headers);
    }

    @SuppressWarnings("unchecked")
    private String loginYObtenerToken(String identificador, String password) {
        var body = Map.of("identificador", identificador, "password", password);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login", body, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }

    @Test
    @DisplayName("debe_responder_200_con_lista_de_vehiculos_del_usuario_autenticado")
    void debe_responder_200_con_lista_de_vehiculos_del_usuario_autenticado() {
        // Arrange — Sembrar un vehículo para el propietario del token y otro para admin
        String placaProp = "PRP" + (System.currentTimeMillis() % 100000);
        var vProp = com.estaciona.api.modules.vehiculos.entity.Vehiculo.builder()
                .placa(placaProp)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Mazda 3")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vProp);

        String placaAdm = "ADM" + (System.currentTimeMillis() % 100000);
        // Obtener usuario admin de la BD
        com.estaciona.api.modules.usuarios.entity.Usuario admin = usuarioRepository.findByCorreoOrDocumento("admin@unicampus.edu.pe", "admin@unicampus.edu.pe").orElseThrow();
        var vAdm = com.estaciona.api.modules.vehiculos.entity.Vehiculo.builder()
                .placa(placaAdm)
                .usuario(admin)
                .tipo("auto")
                .marcaModelo("Volvo XC40")
                .color("Negro")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vAdm);

        // Act — consultar /me como propietario
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/me", HttpMethod.GET, crearRequest(tokenUsuario), Map.class);

        // Assert — 200 OK y sólo debe venir el vehículo de propietario (aislamiento)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        List<?> dataWrapped = (List<?>) response.getBody().get("data");
        assertThat(dataWrapped).isNotEmpty();
        
        // Verificar que ningún elemento de la lista sea el vehículo del administrador
        for (Object obj : dataWrapped) {
            Map<String, Object> veh = (Map<String, Object>) obj;
            assertThat(veh.get("placa")).isNotEqualTo(placaAdm);
        }
    }

    @Test
    @DisplayName("debe_responder_200_con_lista_vacia_si_no_hay_vehiculos")
    void debe_responder_200_con_lista_vacia_si_no_hay_vehiculos() {
        // Arrange — Crear un usuario nuevo sin vehículos
        com.estaciona.api.modules.roles.entity.Rol rolUsr = rolRepository.findByNombre("USUARIO").orElseThrow();
        com.estaciona.api.modules.usuarios.entity.Usuario nuevoUsr = com.estaciona.api.modules.usuarios.entity.Usuario.builder()
                .rol(rolUsr)
                .nombreCompleto("Usuario Sin Vehiculo")
                .correo("sin.vehiculo@unicampus.edu.pe")
                .documento("USR0888")
                .passwordHash(passwordEncoder.encode("Usuario123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(nuevoUsr);
        String tokenNuevo = "Bearer " + loginYObtenerToken("sin.vehiculo@unicampus.edu.pe", "Usuario123!");

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/me", HttpMethod.GET, crearRequest(tokenNuevo), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> data = (List<?>) response.getBody().get("data");
        assertThat(data).isEmpty();
    }

    @Test
    @DisplayName("debe_responder_200_al_buscar_placa_existente_como_seguridad")
    void debe_responder_200_al_buscar_placa_existente_como_seguridad() {
        // Arrange — sembrar vehículo
        String placa = "BUS" + (System.currentTimeMillis() % 100000);
        var v = com.estaciona.api.modules.vehiculos.entity.Vehiculo.builder()
                .placa(placa)
                .usuario(propietario)
                .tipo("moto")
                .marcaModelo("Yamaha R3")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(v);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/buscar/" + placa, HttpMethod.GET, crearRequest(tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("placa")).isEqualTo(placa);
        assertThat(data.get("propietarioNombre")).isEqualTo("Propietario Vehiculos");
        assertThat(data.get("propietarioDocumento")).isEqualTo("USR0099");
        assertThat(data.get("propietarioCorreo")).isEqualTo("propietario.vehiculos@unicampus.edu.pe");
    }

    @Test
    @DisplayName("debe_responder_403_al_buscar_placa_con_rol_usuario")
    void debe_responder_403_al_buscar_placa_con_rol_usuario() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/buscar/HABIL01", HttpMethod.GET, crearRequest(tokenUsuario), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_403_al_buscar_placa_con_rol_administrador")
    void debe_responder_403_al_buscar_placa_con_rol_administrador() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/buscar/HABIL01", HttpMethod.GET, crearRequest(tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_401_si_no_se_envia_token_en_me")
    void debe_responder_401_si_no_se_envia_token_en_me() {
        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(URL + "/me", Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("debe_responder_404_al_buscar_placa_inexistente_como_seguridad")
    void debe_responder_404_al_buscar_placa_inexistente_como_seguridad() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/buscar/INEX123", HttpMethod.GET, crearRequest(tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("debe_actualizar_vehiculo_propio_ok")
    void debe_actualizar_vehiculo_propio_ok() {
        // Arrange
        String placa = "UPDT" + (System.currentTimeMillis() % 10000);
        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Toyota Yaris")
                .color("Blanco")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vehiculo);

        var body = Map.of(
                "tipo", "auto",
                "marcaModelo", "Toyota Yaris Modificado",
                "color", "Gris"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + vehiculo.getId(), HttpMethod.PUT, crearRequest(body, tokenUsuario), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("marcaModelo")).isEqualTo("Toyota Yaris Modificado");
        assertThat(data.get("color")).isEqualTo("Gris");
    }

    @Test
    @DisplayName("debe_lanzar_403_si_actualiza_vehiculo_ajeno")
    void debe_lanzar_403_si_actualiza_vehiculo_ajeno() {
        // Arrange
        String placa = "AJEN" + (System.currentTimeMillis() % 10000);
        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Toyota")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vehiculo);

        var body = Map.of(
                "tipo", "auto",
                "marcaModelo", "Toyota Modificado",
                "color", "Rojo"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + vehiculo.getId(), HttpMethod.PUT, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_eliminar_vehiculo_soft_delete_ok")
    void debe_eliminar_vehiculo_soft_delete_ok() {
        // Arrange
        String placa = "DELT" + (System.currentTimeMillis() % 10000);
        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Nissan Sentra")
                .color("Negro")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vehiculo);

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                URL + "/" + vehiculo.getId(), HttpMethod.DELETE, crearRequest(null, tokenUsuario), Void.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar en BD
        Vehiculo vDb = vehiculoRepository.findById(vehiculo.getId()).orElseThrow();
        assertThat(vDb.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("debe_lanzar_422_si_vehiculo_tiene_acceso_activo")
    void debe_lanzar_422_si_vehiculo_tiene_acceso_activo() {
        // Arrange
        com.estaciona.api.modules.campus.entity.Campus campus = new com.estaciona.api.modules.campus.entity.Campus();
        campus.setNombre("Campus Sur");
        campus.setEnabled(true);
        campusRepository.saveAndFlush(campus);

        Zona zona = Zona.builder()
                .campus(campus)
                .nombre("Zona Sur A")
                .tipo("interno")
                .aforoMaximo(10)
                .aforoDisponible(9)
                .estado("activa")
                .enabled(true)
                .build();
        zonaRepository.saveAndFlush(zona);

        String placa = "ACTV" + (System.currentTimeMillis() % 10000);
        Vehiculo vehiculo = Vehiculo.builder()
                .placa(placa)
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Kia Rio")
                .color("Rojo")
                .enabled(true)
                .build();
        vehiculoRepository.saveAndFlush(vehiculo);

        com.estaciona.api.modules.roles.entity.Rol rolSeg = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
        Usuario guardia = Usuario.builder()
                .rol(rolSeg)
                .nombreCompleto("Guardia Acceso Activo")
                .correo("guardia.activo@unicampus.edu.pe")
                .documento("SEC8888")
                .passwordHash(passwordEncoder.encode("Guardia123!"))
                .enabled(true)
                .build();
        usuarioRepository.saveAndFlush(guardia);

        com.estaciona.api.modules.accesos.entity.AccesoVehicular acceso = com.estaciona.api.modules.accesos.entity.AccesoVehicular.builder()
                .usuario(propietario)
                .vehiculo(vehiculo)
                .zona(zona)
                .guardiaEntrada(guardia)
                .horaIngreso(java.time.OffsetDateTime.now())
                .estado("en_curso")
                .enabled(true)
                .build();
        accesoVehicularRepository.saveAndFlush(acceso);

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + vehiculo.getId(), HttpMethod.DELETE, crearRequest(null, tokenUsuario), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
