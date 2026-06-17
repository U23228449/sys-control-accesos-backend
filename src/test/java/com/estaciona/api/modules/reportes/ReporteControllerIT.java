package com.estaciona.api.modules.reportes;

import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReporteControllerIT extends AbstractIntegrationTest {

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
    private AccesoVehicularRepository accesoVehicularRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String URL = "/api/v1/reportes/accesos";

    private String tokenCoordinador;
    private String tokenSeguridad;
    private String tokenUsuario;

    private Zona zonaA;
    private Zona zonaB;
    private Usuario guardia;
    private AccesoVehicular accesoCompletado;
    private AccesoVehicular accesoEnCurso;

    @BeforeAll
    void setupDatosReportes() {
        // 1. Obtener roles
        Rol rolCoordinador = rolRepository.findByNombre("COORDINADOR_SEGURIDAD").orElseThrow();
        Rol rolSeguridad = rolRepository.findByNombre("SEGURIDAD").orElseThrow();
        Rol rolUsuario = rolRepository.findByNombre("USUARIO").orElseThrow();

        // 2. Crear usuarios
        Usuario coordinador = Usuario.builder()
                .rol(rolCoordinador)
                .nombreCompleto("Coordinador Reportes")
                .correo("coordinador.reportes@unicampus.edu.pe")
                .documento("COORD99")
                .passwordHash(passwordEncoder.encode("Coord123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(coordinador);

        guardia = Usuario.builder()
                .rol(rolSeguridad)
                .nombreCompleto("Guardia Reportes")
                .correo("guardia.reportes@unicampus.edu.pe")
                .documento("SEC9999")
                .passwordHash(passwordEncoder.encode("Guardia123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(guardia);

        Usuario propietario = Usuario.builder()
                .rol(rolUsuario)
                .nombreCompleto("Propietario Reportes")
                .correo("propietario.reportes@unicampus.edu.pe")
                .documento("USR9999")
                .passwordHash(passwordEncoder.encode("Usuario123!"))
                .enabled(true)
                .build();
        usuarioRepository.save(propietario);

        // 3. Obtener tokens JWT
        tokenCoordinador = "Bearer " + loginYObtenerToken("coordinador.reportes@unicampus.edu.pe", "Coord123!");
        tokenSeguridad = "Bearer " + loginYObtenerToken("guardia.reportes@unicampus.edu.pe", "Guardia123!");
        tokenUsuario = "Bearer " + loginYObtenerToken("propietario.reportes@unicampus.edu.pe", "Usuario123!");

        // 4. Crear Campus y Zonas
        Campus campus = new Campus();
        campus.setNombre("Campus Norte");
        campus.setEnabled(true);
        campusRepository.save(campus);

        zonaA = Zona.builder()
                .campus(campus)
                .nombre("Zona Principal A")
                .tipo("interno")
                .aforoMaximo(20)
                .aforoDisponible(18)
                .estado("activa")
                .enabled(true)
                .build();
        zonaRepository.save(zonaA);

        zonaB = Zona.builder()
                .campus(campus)
                .nombre("Zona Auxiliar B")
                .tipo("externo")
                .aforoMaximo(10)
                .aforoDisponible(9)
                .estado("activa")
                .enabled(true)
                .build();
        zonaRepository.save(zonaB);

        // 5. Crear Vehículos
        Vehiculo v1 = Vehiculo.builder()
                .placa("REP-001")
                .usuario(propietario)
                .tipo("auto")
                .marcaModelo("Hyundai Tucson")
                .color("Rojo")
                .enabled(true)
                .build();
        vehiculoRepository.save(v1);

        Vehiculo v2 = Vehiculo.builder()
                .placa("REP-002")
                .usuario(propietario)
                .tipo("moto")
                .marcaModelo("Yamaha FZ25")
                .color("Azul")
                .enabled(true)
                .build();
        vehiculoRepository.save(v2);

        // 6. Crear Accesos Vehiculares
        accesoCompletado = AccesoVehicular.builder()
                .usuario(propietario)
                .vehiculo(v1)
                .zona(zonaA)
                .guardiaEntrada(guardia)
                .guardiaSalida(guardia)
                .horaIngreso(OffsetDateTime.now().minusHours(3))
                .horaSalida(OffsetDateTime.now().minusHours(1))
                .estado("completada")
                .enabled(true)
                .build();
        accesoVehicularRepository.save(accesoCompletado);

        accesoEnCurso = AccesoVehicular.builder()
                .usuario(propietario)
                .vehiculo(v2)
                .zona(zonaB)
                .guardiaEntrada(guardia)
                .guardiaSalida(null)
                .horaIngreso(OffsetDateTime.now().minusMinutes(30))
                .horaSalida(null)
                .estado("en_curso")
                .enabled(true)
                .build();
        accesoVehicularRepository.save(accesoEnCurso);
    }

    @Test
    @DisplayName("debe_responder_200_y_filtrar_correctamente_por_zona_y_rango_de_fechas")
    void debe_responder_200_y_filtrar_correctamente_por_zona_y_rango_de_fechas() {
        // Act
        String urlConFiltros = URL + "?zonaId=" + zonaA.getId() + "&desde=" + OffsetDateTime.now().minusDays(1);
        ResponseEntity<Map> response = restTemplate.exchange(
                urlConFiltros, HttpMethod.GET, crearRequest(tokenCoordinador), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("content")).isNotNull();
        java.util.List<?> content = (java.util.List<?>) data.get("content");
        assertThat(content).hasSize(1);
    }

    @Test
    @DisplayName("debe_responder_200_y_paginar_resultados")
    void debe_responder_200_y_paginar_resultados() {
        // Act
        ResponseEntity<Map> response = restTemplate.exchange(
                URL + "?page=0&size=1", HttpMethod.GET, crearRequest(tokenCoordinador), Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        java.util.List<?> content = (java.util.List<?>) data.get("content");
        assertThat(content).hasSize(1);
    }

    @Test
    @DisplayName("debe_responder_403_si_rol_no_es_coordinador_seguridad")
    void debe_responder_403_si_rol_no_es_coordinador_seguridad() {
        // Act - Seguridad no tiene permisos para reportes
        ResponseEntity<Map> responseSeg = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(tokenSeguridad), Map.class);

        // Assert
        assertThat(responseSeg.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Act - Usuario regular no tiene permisos
        ResponseEntity<Map> responseUsr = restTemplate.exchange(
                URL, HttpMethod.GET, crearRequest(tokenUsuario), Map.class);

        // Assert
        assertThat(responseUsr.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("debe_responder_200_con_content_type_xlsx_y_content_disposition_attachment")
    void debe_responder_200_con_content_type_xlsx_y_content_disposition_attachment() {
        // Act
        ResponseEntity<byte[]> response = restTemplate.exchange(
                URL + "/exportar", HttpMethod.GET, crearRequest(tokenCoordinador), byte[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename()).startsWith("reporte-accesos-");
    }

    @Test
    @DisplayName("debe_generar_excel_descargable_con_las_filas_filtradas")
    void debe_generar_excel_descargable_con_las_filas_filtradas() throws Exception {
        // Act - Filtrar para que solo venga el acceso en curso (Zona B)
        String urlExportar = URL + "/exportar?zonaId=" + zonaB.getId();
        ResponseEntity<byte[]> response = restTemplate.exchange(
                urlExportar, HttpMethod.GET, crearRequest(tokenCoordinador), byte[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] body = response.getBody();
        assertThat(body).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(body))) {
            Sheet sheet = workbook.getSheet("Accesos");
            assertThat(sheet).isNotNull();

            // Titulo (fila 0), Spacing (fila 1), Encabezados (fila 2), Fila 3 es el primer dato
            Row headerRow = sheet.getRow(2);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Placa");

            // Solo debe haber 1 fila de datos correspondiente a Zona B (REP-002)
            Row dataRow1 = sheet.getRow(3);
            assertThat(dataRow1.getCell(0).getStringCellValue()).isEqualTo("REP-002");
            assertThat(dataRow1.getCell(1).getStringCellValue()).isEqualTo("moto");
            assertThat(dataRow1.getCell(4).getStringCellValue()).isEqualTo("Zona Auxiliar B");
            assertThat(dataRow1.getCell(10).getStringCellValue()).isEqualTo("en_curso");

            // La fila 4 debe estar vacía porque solo hay 1 registro
            assertThat(sheet.getRow(4)).isNull();
        }
    }

    @Test
    @DisplayName("debe_responder_200_con_excel_vacio_pero_valido_si_no_hay_resultados")
    void debe_responder_200_con_excel_vacio_pero_valido_si_no_hay_resultados() throws Exception {
        // Act - Filtrar por una zona inexistente (ID = 9999)
        ResponseEntity<byte[]> response = restTemplate.exchange(
                URL + "/exportar?zonaId=9999", HttpMethod.GET, crearRequest(tokenCoordinador), byte[].class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] body = response.getBody();
        assertThat(body).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(body))) {
            Sheet sheet = workbook.getSheet("Accesos");
            assertThat(sheet).isNotNull();

            // Verificar que los encabezados existan
            Row headerRow = sheet.getRow(2);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Placa");

            // Verificar que no haya filas de datos
            assertThat(sheet.getRow(3)).isNull();
        }
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
}
