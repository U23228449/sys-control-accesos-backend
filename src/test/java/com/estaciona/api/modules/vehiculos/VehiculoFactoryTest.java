package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de VehiculoFactory — sin Spring context (POJO test).
 */
class VehiculoFactoryTest {

    private VehiculoFactory factory;
    private Usuario propietario;

    @BeforeEach
    void setUp() {
        factory = new VehiculoFactory();

        Rol rolUsuario = new Rol();
        rolUsuario.setId(4);
        rolUsuario.setNombre("USUARIO");

        propietario = new Usuario();
        propietario.setId(UUID.randomUUID());
        propietario.setNombreCompleto("Juan Pérez");
        propietario.setRol(rolUsuario);
        propietario.setEnabled(true);
    }

    @Test
    @DisplayName("debe_construir_vehiculo_con_enabled_true_por_defecto")
    void debe_construir_vehiculo_con_enabled_true_por_defecto() {
        // Arrange
        var request = new VehiculoRequest("auto", "ABC123", "Toyota Corolla", "Blanco");

        // Act
        Vehiculo vehiculo = factory.crear(request, propietario);

        // Assert
        assertThat(vehiculo.isEnabled()).isTrue();
        assertThat(vehiculo.getUsuario()).isEqualTo(propietario);
    }

    @Test
    @DisplayName("debe_normalizar_placa_a_mayusculas_sin_espacios")
    void debe_normalizar_placa_a_mayusculas_sin_espacios() {
        // Arrange — placa en minúsculas con espacios
        var request = new VehiculoRequest("auto", "abc 123", "Honda Civic", "Rojo");

        // Act
        Vehiculo vehiculo = factory.crear(request, propietario);

        // Assert
        assertThat(vehiculo.getPlaca()).isEqualTo("ABC123");
    }

    @Test
    @DisplayName("debe_normalizar_tipo_a_minusculas")
    void debe_normalizar_tipo_a_minusculas() {
        // Arrange — tipo en mayúsculas (el @Pattern acepta ambos)
        var request = new VehiculoRequest("AUTO", "XYZ789", "Kawasaki Ninja", "Negro");

        // Act
        Vehiculo vehiculo = factory.crear(request, propietario);

        // Assert
        assertThat(vehiculo.getTipo()).isEqualTo("auto");
    }
}
