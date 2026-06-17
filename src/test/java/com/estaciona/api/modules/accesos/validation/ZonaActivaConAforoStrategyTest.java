package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZonaActivaConAforoStrategyTest {

    private final ZonaActivaConAforoStrategy strategy = new ZonaActivaConAforoStrategy();

    @Test
    @DisplayName("validar_no_lanza_excepcion_cuando_zona_esta_activa_con_aforo")
    void validar_no_lanza_excepcion_cuando_zona_esta_activa_con_aforo() {
        Zona zona = Zona.builder().estado("activa").aforoDisponible(5).build();
        assertThatCode(() -> strategy.validar(null, zona)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validar_lanza_422_cuando_zona_esta_cerrada")
    void validar_lanza_422_cuando_zona_esta_cerrada() {
        Zona zona = Zona.builder().estado("cerrada").aforoDisponible(5).build();
        assertThatThrownBy(() -> strategy.validar(null, zona))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La zona de estacionamiento está cerrada.");
    }

    @Test
    @DisplayName("validar_lanza_422_cuando_zona_no_tiene_aforo_disponible")
    void validar_lanza_422_cuando_zona_no_tiene_aforo_disponible() {
        Zona zona = Zona.builder().estado("activa").aforoDisponible(0).build();
        assertThatThrownBy(() -> strategy.validar(null, zona))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La zona de estacionamiento no cuenta con aforo disponible.");
    }
}
