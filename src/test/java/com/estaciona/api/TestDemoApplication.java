package com.estaciona.api;

import org.springframework.boot.SpringApplication;

/**
 * Punto de entrada de la aplicación en modo test (con Testcontainers activos).
 */
public class TestDemoApplication {

    public static void main(String[] args) {
        SpringApplication.from(EstacionaApiApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
