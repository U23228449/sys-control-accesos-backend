package com.estaciona.api;

import com.estaciona.api.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Test de smoke: verifica que el contexto de Spring se levanta correctamente
 * con todos los beans y la BD real (Testcontainers).
 */
class EstacionaApiApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Si el contexto se levanta, las migraciones Flyway corren y la BD está ok
    }
}
