package com.estaciona.api.modules.accesos;

import com.estaciona.api.modules.campus.CampusRepository;
import com.estaciona.api.modules.campus.entity.Campus;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.VehiculoRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.entity.Zona;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccesoVehicularControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccesoVehicularRepository accesoVehicularRepository;

    private static final String URL = "/api/v1/accesos-vehiculares";

    private String tokenSeguridad;
    private String tokenAdmin; // for 403 checks, since only SEGURIDAD has access to these endpoints
    private String tokenUsuario;

    private Vehiculo vehiculoHabilitado;
    private Vehiculo vehiculoDeshabilitado;

    private Zona zonaActiva;
    private Zona zonaCerrada;
    private Zona zonaSinAforo;

    @BeforeAll
    void setupDatosIT() {
        // 1. Obtener roles
        Rol rolSeguridad = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR").orElseThrow();

        // 2. Crear usuarios
        Usuario guardia = Usuario.builder()
                .rol(rolSeguridad)
                .nombreCompleto("Guardia Acceso")
                .correo("guardia.acceso@unicampus.edu.pe")
                .documento("SEC0001")
                .passwordHash(passwordEncoder.encode("Guardia123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(guardia);

        Usuario propietario = Usuario.builder()
                .rol(rolUsuario)
                .nombreCompleto("Propietario Acceso")
                .correo("propietario.acceso@unicampus.edu.pe")
                .documento("USR0001")
                .passwordHash(passwordEncoder.encode("Usuario123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(propietario);

        // 3. Obtener tokens JWT
        tokenSeguridad = "Bearer " + loginYObtenerToken("guardia.acceso@unicampus.edu.pe", "Guardia123!");
        tokenAdmin = "Bearer " + loginYObtenerToken("admin@unicampus.edu.pe", "Admin123!");
        tokenUsuario = "Bearer " + loginYObtenerToken("propietario.acceso@unicampus.edu.pe", "Usuario123!");

        // 4. Crear vehículos
        vehiculoHabilitado = Vehiculo.builder()
                .placa("HABIL01")
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Hyundai Elantra")
                .color("Gris")
                .enabled(true)
                .build();
        vehiculoRepository.save(vehiculoHabilitado);

        vehiculoDeshabilitado = Vehiculo.builder()
                .placa("DESHA01")
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Nissan Sentra")
                .color("Negro")
                .enabled(false)
                .build();
        vehiculoRepository.save(vehiculoDeshabilitado);

        // 5. Obtener campus y crear zonas
        Campus campus = campusRepository.findAll().stream().findFirst().orElseThrow();

        zonaActiva = Zona.builder()
                .campus(campus)
                .nombre("Zona Activa")
                .ubicacion("Puerta 1")
                .tipo("auto")
                .aforoMaximo(5)
                .aforoDisponible(5)
                .estado("activa")
                .enabled(true)
                .build();
        zonaRepository.save(zonaActiva);

        zonaCerrada = Zona.builder()
                .campus(campus)
                .nombre("Zona Cerrada")
                .ubicacion("Puerta 2")
                .tipo("auto")
                .aforoMaximo(5)
                .aforoDisponible(5)
                .estado("cerrada")
                .enabled(true)
                .build();
        zonaRepository.save(zonaCerrada);

        zonaSinAforo = Zona.builder()
                .campus(campus)
                .nombre("Zona Sin Aforo")
                .ubicacion("Puerta 3")
                .tipo("auto")
                .aforoMaximo(1)
                .aforoDisponible(0)
                .estado("activa")
                .enabled(true)
                .build();
        zonaRepository.save(zonaSinAforo);
    }

    @Test
    @DisplayName("debe_responder_201_y_decrementar_aforo_de_la_zona")
    void debe_responder_201_y_decrementar_aforo_de_la_zona() {
        // Arrange
        var body = Map.of(
                "placa", vehiculoHabilitado.getPlaca(),
                "zonaId", zonaActiva.getId()
        );

        // Obtener aforo disponible inicial
        int aforoInicial = zonaRepository.findById(zonaActiva.getId()).orElseThrow().getAforoDisponible();

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("id")).isNotNull();
        assertThat(data.get("placa")).isEqualTo(vehiculoHabilitado.getPlaca());
        assertThat(data.get("propietario")).isEqualTo(vehiculoHabilitado.getUsuario().getNombreCompleto());
        assertThat(data.get("zona")).isEqualTo(zonaActiva.getNombre());
        assertThat(data.get("estado")).isEqualTo("en_curso");

        // Verificar decremento de aforo en base de datos
        int aforoFinal = zonaRepository.findById(zonaActiva.getId()).orElseThrow().getAforoDisponible();
        assertThat(aforoFinal).isEqualTo(aforoInicial - 1);
    }

    @Test
    @DisplayName("debe_responder_403_si_quien_registra_no_es_seguridad")
    void debe_responder_403_si_quien_registra_no_es_seguridad() {
        // Arrange — usando token de ADMINISTRADOR
        var body = Map.of(
                "placa", "CUALQUIERA",
                "zonaId", zonaActiva.getId()
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenAdmin), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_404_si_placa_no_existe")
    void debe_responder_404_si_placa_no_existe() {
        // Arrange
        var body = Map.of(
                "placa", "NOEXISTE",
                "zonaId", zonaActiva.getId()
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("debe_responder_403_si_vehiculo_deshabilitado")
    void debe_responder_403_si_vehiculo_deshabilitado() {
        // Arrange
        var body = Map.of(
                "placa", vehiculoDeshabilitado.getPlaca(),
                "zonaId", zonaActiva.getId()
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().get("error")).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("debe_responder_422_si_zona_cerrada")
    void debe_responder_422_si_zona_cerrada() {
        // Arrange
        var body = Map.of(
                "placa", vehiculoHabilitado.getPlaca(),
                "zonaId", zonaCerrada.getId()
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("debe_responder_422_si_zona_sin_aforo")
    void debe_responder_422_si_zona_sin_aforo() {
        // Arrange
        var body = Map.of(
                "placa", vehiculoHabilitado.getPlaca(),
                "zonaId", zonaSinAforo.getId()
        );

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().get("error")).isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("debe_responder_422_si_ya_existe_acceso_en_curso_para_el_vehiculo")
    void debe_responder_422_si_ya_existe_acceso_en_curso_para_el_vehiculo() {
        // Arrange — registrar primer ingreso
        String placaUnica = "REP00" + (System.currentTimeMillis() % 1000);
        Vehiculo vehiculoUnico = Vehiculo.builder()
                .placa(placaUnica)
                .usuario(vehiculoHabilitado.getUsuario())
                .tipo("auto")
                .marcaModelo("Hyundai")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.save(vehiculoUnico);

        var body = Map.of(
                "placa", placaUnica,
                "zonaId", zonaActiva.getId()
        );

        // Registrar primer ingreso
        ResponseEntity<Map> response1 = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Act — intentar registrar segundo ingreso
        ResponseEntity<Map> response2 = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);

        // Assert
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response2.getBody().get("error")).isEqualTo("UNPROCESSABLE_ENTITY");
    }

    @Test
    @DisplayName("debe_responder_200_y_completar_acceso_existente_en_curso_y_devolver_aforo")
    void debe_responder_200_y_completar_acceso_existente_en_curso_y_devolver_aforo() {
        // Arrange — registrar ingreso primero
        String placaUnica = "OUT00" + (System.currentTimeMillis() % 1000);
        Vehiculo vehiculoUnico = Vehiculo.builder()
                .placa(placaUnica)
                .usuario(vehiculoHabilitado.getUsuario())
                .tipo("auto")
                .marcaModelo("Hyundai")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.save(vehiculoUnico);

        var body = Map.of(
                "placa", placaUnica,
                "zonaId", zonaActiva.getId()
        );

        ResponseEntity<Map> responseIngreso = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);
        assertThat(responseIngreso.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        @SuppressWarnings("unchecked")
        Map<String, Object> dataIngreso = (Map<String, Object>) responseIngreso.getBody().get("data");
        String accesoId = (String) dataIngreso.get("id");

        int aforoDespuesIngreso = zonaRepository.findById(zonaActiva.getId()).orElseThrow().getAforoDisponible();

        // Act — registrar salida
        ResponseEntity<Map> responseSalida = restTemplate.exchange(
                URL + "/" + accesoId + "/salida", HttpMethod.PATCH, crearRequest(null, tokenSeguridad), Map.class);

        // Assert
        assertThat(responseSalida.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> dataSalida = (Map<String, Object>) responseSalida.getBody().get("data");
        assertThat(dataSalida.get("id")).isEqualTo(accesoId);
        assertThat(dataSalida.get("estado")).isEqualTo("completada");
        assertThat(dataSalida.get("horaSalida")).isNotNull();
        assertThat(dataSalida.get("guardiaSalida")).isEqualTo("Guardia Acceso");

        // Verificar incremento de aforo
        int aforoDespuesSalida = zonaRepository.findById(zonaActiva.getId()).orElseThrow().getAforoDisponible();
        assertThat(aforoDespuesSalida).isEqualTo(aforoDespuesIngreso + 1);
    }

    @Test
    @DisplayName("debe_responder_404_si_id_de_acceso_no_existe")
    void debe_responder_404_si_id_de_acceso_no_existe() {
        // Arrange
        UUID fakeAccesoId = UUID.randomUUID();

        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + fakeAccesoId + "/salida", HttpMethod.PATCH, crearRequest(null, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("debe_responder_409_si_acceso_ya_fue_cerrado_previamente")
    void debe_responder_409_si_acceso_ya_fue_cerrado_previamente() {
        // Arrange — registrar ingreso
        String placaUnica = "OUTX" + (System.currentTimeMillis() % 1000);
        Vehiculo vehiculoUnico = Vehiculo.builder()
                .placa(placaUnica)
                .usuario(vehiculoHabilitado.getUsuario())
                .tipo("auto")
                .marcaModelo("Hyundai")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.save(vehiculoUnico);

        var body = Map.of(
                "placa", placaUnica,
                "zonaId", zonaActiva.getId()
        );

        ResponseEntity<Map> responseIngreso = restTemplate.exchange(
                URL, HttpMethod.POST, crearRequest(body, tokenSeguridad), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> dataIngreso = (Map<String, Object>) responseIngreso.getBody().get("data");
        String accesoId = (String) dataIngreso.get("id");

        // Registrar primera salida
        restTemplate.exchange(URL + "/" + accesoId + "/salida", HttpMethod.PATCH, crearRequest(null, tokenSeguridad), Map.class);

        // Act — intentar registrar segunda salida
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "/" + accesoId + "/salida", HttpMethod.PATCH, crearRequest(null, tokenSeguridad), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("error")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("debe_responder_200_con_historial_paginado")
    void debe_responder_200_con_historial_paginado() {
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(null, tokenAdmin), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertThat(data.get("content")).isNotNull();
    }

    @Test
    @DisplayName("debe_filtrar_por_estado_completada")
    void debe_filtrar_por_estado_completada() {
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "?estado=completada", HttpMethod.GET, crearRequest(null, tokenAdmin), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = response.getBody();
        assertThat(body).isNotNull();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertThat(data.get("content")).isNotNull();
    }

    @Test
    @DisplayName("debe_responder_403_si_no_es_administrador")
    void debe_responder_403_si_no_es_administrador() {
        ResponseEntity<Map> response = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(null, tokenUsuario), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
