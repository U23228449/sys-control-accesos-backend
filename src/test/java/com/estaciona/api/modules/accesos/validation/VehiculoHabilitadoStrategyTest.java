package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.ForbiddenOperationException;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehiculoHabilitadoStrategyTest {

    private final VehiculoHabilitadoStrategy strategy = new VehiculoHabilitadoStrategy();

    @Test
    @DisplayName("validar_no_lanza_excepcion_cuando_vehiculo_esta_habilitado")
    void validar_no_lanza_excepcion_cuando_vehiculo_esta_habilitado() {
        Vehiculo vehiculo = Vehiculo.builder().enabled(true).build();
        assertThatCode(() -> strategy.validar(vehiculo, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validar_lanza_403_cuando_vehiculo_esta_deshabilitado")
    void validar_lanza_403_cuando_vehiculo_esta_deshabilitado() {
        Vehiculo vehiculo = Vehiculo.builder().enabled(false).build();
        assertThatThrownBy(() -> strategy.validar(vehiculo, null))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Vehículo deshabilitado");
    }
}
