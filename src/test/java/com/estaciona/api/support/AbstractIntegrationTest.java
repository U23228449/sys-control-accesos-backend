package com.estaciona.api.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Clase base para todos los tests de integración (*ControllerIT).
 *
 * Patrón Singleton real: el contenedor se inicia UNA SOLA VEZ en el bloque
 * estático cuando la JVM carga la clase, y se comparte entre TODAS las
 * subclases durante toda la ejecución del test suite.
 *
 * Esto evita que @Testcontainers detenga/arranque el contenedor entre clases,
 * lo cual causa fallos cuando una subclase usa @TestInstance(PER_CLASS).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    /** Contenedor PostgreSQL iniciado una sola vez para toda la suite. */
    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("estaciona_test")
                .withUsername("test_user")
                .withPassword("test_pass");
        POSTGRES.start();
    }

    /**
     * Sobreescribe las propiedades del datasource para que Spring Boot
     * apunte al contenedor de Testcontainers en lugar de la BD de desarrollo.
     */
    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
        // Secreto JWT fijo para tests — suficientemente largo para HS256
        registry.add("jwt.secret", () -> "test-secret-key-must-be-at-least-32-bytes-long!!");
        registry.add("jwt.expiration-minutes", () -> "60");
    }
}
