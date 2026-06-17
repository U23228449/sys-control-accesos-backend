package com.estaciona.api.modules.auditoria;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.auditoria.dto.AuditoriaEventoRequest;
import com.estaciona.api.modules.auditoria.entity.LogAuditoria;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditoriaEventoFactoryTest {

    private final AuditoriaEventoFactory factory = new AuditoriaEventoFactory();

    @Test
    @DisplayName("debe_construir_evento_insert_valido")
    void debe_construir_evento_insert_valido() {
        // Arrange
        var request = new AuditoriaEventoRequest(
                "vehiculos",
                "12345678-1234-1234-1234-1234567890ab",
                "INSERT",
                null,
                "{\"placa\": \"ABC123\"}"
        );
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());

        // Act
        LogAuditoria log = factory.crear(request, usuario);

        // Assert
        assertThat(log.getTablaAfectada()).isEqualTo("vehiculos");
        assertThat(log.getRegistroId()).isEqualTo("12345678-1234-1234-1234-1234567890ab");
        assertThat(log.getAccion()).isEqualTo("INSERT");
        assertThat(log.getValoresAnteriores()).isNull();
        assertThat(log.getValoresNuevos()).isEqualTo("{\"placa\": \"ABC123\"}");
        assertThat(log.getUsuario()).isEqualTo(usuario);
        assertThat(log.getFecha()).isNotNull();
    }

    @Test
    @DisplayName("debe_lanzar_422_si_accion_es_invalida")
    void debe_lanzar_422_si_accion_es_invalida() {
        // Arrange
        var request = new AuditoriaEventoRequest(
                "vehiculos", "id123", "INVALIDO", null, null);

        // Act & Assert
        assertThatThrownBy(() -> factory.crear(request, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La acción de auditoría debe ser INSERT, UPDATE o DELETE.");
    }

    @Test
    @DisplayName("debe_lanzar_422_si_insert_tiene_valores_anteriores")
    void debe_lanzar_422_si_insert_tiene_valores_anteriores() {
        // Arrange
        var request = new AuditoriaEventoRequest(
                "vehiculos", "id123", "INSERT", "{\"color\":\"rojo\"}", "{\"color\":\"azul\"}");

        // Act & Assert
        assertThatThrownBy(() -> factory.crear(request, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Un evento INSERT no puede contener valores anteriores.");
    }

    @Test
    @DisplayName("debe_lanzar_422_si_delete_tiene_valores_nuevos")
    void debe_lanzar_422_si_delete_tiene_valores_nuevos() {
        // Arrange
        var request = new AuditoriaEventoRequest(
                "vehiculos", "id123", "DELETE", "{\"color\":\"rojo\"}", "{\"color\":\"azul\"}");

        // Act & Assert
        assertThatThrownBy(() -> factory.crear(request, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Un evento DELETE no puede contener valores nuevos.");
    }
}
