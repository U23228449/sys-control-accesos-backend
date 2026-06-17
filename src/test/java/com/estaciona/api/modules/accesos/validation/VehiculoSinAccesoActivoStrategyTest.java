package com.estaciona.api.modules.accesos.validation;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehiculoSinAccesoActivoStrategyTest {

    @Mock
    private AccesoVehicularRepository repository;

    @InjectMocks
    private VehiculoSinAccesoActivoStrategy strategy;

    @Test
    @DisplayName("validar_no_lanza_excepcion_cuando_vehiculo_no_tiene_acceso_activo")
    void validar_no_lanza_excepcion_cuando_vehiculo_no_tiene_acceso_activo() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = Vehiculo.builder().id(vehiculoId).build();

        when(repository.findByVehiculoIdAndEstado(vehiculoId, "en_curso")).thenReturn(Optional.empty());

        assertThatCode(() -> strategy.validar(vehiculo, null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validar_lanza_422_cuando_vehiculo_ya_tiene_acceso_en_curso")
    void validar_lanza_422_cuando_vehiculo_ya_tiene_acceso_en_curso() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = Vehiculo.builder().id(vehiculoId).build();
        AccesoVehicular acceso = AccesoVehicular.builder().build();

        when(repository.findByVehiculoIdAndEstado(vehiculoId, "en_curso")).thenReturn(Optional.of(acceso));

        assertThatThrownBy(() -> strategy.validar(vehiculo, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("El vehículo ya tiene un acceso en curso.");
    }
}
