package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionUpdateRequest;
import com.estaciona.api.modules.configuraciones.entity.Configuracion;
import com.estaciona.api.modules.configuraciones.event.ConfiguracionCambiadaEvent;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionValidationStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionRepository configuracionRepository;

    @Mock
    private ConfiguracionHandlerFactory handlerFactory;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConfiguracionServiceImpl configuracionService;

    @Test
    @DisplayName("debe_listar_solo_configuraciones_habilitadas")
    void debe_listar_solo_configuraciones_habilitadas() {
        // Arrange
        Configuracion c1 = new Configuracion();
        c1.setId(1);
        c1.setClave("jwt.expiration");
        c1.setValor("3600");
        c1.setEnabled(true);

        Configuracion c2 = new Configuracion();
        c2.setId(2);
        c2.setClave("system.maintenance");
        c2.setValor("true");
        c2.setEnabled(false);

        when(configuracionRepository.findAll()).thenReturn(List.of(c1, c2));

        // Act
        List<ConfiguracionResponse> result = configuracionService.listarConfiguraciones();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).clave()).isEqualTo("jwt.expiration");
    }

    @Test
    @DisplayName("debe_actualizar_configuracion_y_publicar_evento")
    void debe_actualizar_configuracion_y_publicar_evento() {
        // Arrange
        Configuracion config = new Configuracion();
        config.setId(1);
        config.setClave("jwt.expiration");
        config.setValor("3600");
        config.setEnabled(true);

        ConfiguracionUpdateRequest request = new ConfiguracionUpdateRequest("7200");
        ConfiguracionValidationStrategy mockStrategy = mock(ConfiguracionValidationStrategy.class);

        when(configuracionRepository.findByClaveAndEnabledTrue("jwt.expiration")).thenReturn(Optional.of(config));
        when(handlerFactory.resolver("jwt.expiration")).thenReturn(mockStrategy);
        when(configuracionRepository.save(any(Configuracion.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ConfiguracionResponse response = configuracionService.actualizarConfiguracion("jwt.expiration", request);

        // Assert
        assertThat(response.valor()).isEqualTo("7200");
        verify(mockStrategy, times(1)).validar("jwt.expiration", "7200");
        verify(configuracionRepository, times(1)).save(config);

        ArgumentCaptor<ConfiguracionCambiadaEvent> captor = ArgumentCaptor.forClass(ConfiguracionCambiadaEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        ConfiguracionCambiadaEvent event = captor.getValue();
        assertThat(event.getClave()).isEqualTo("jwt.expiration");
        assertThat(event.getValorAnterior()).isEqualTo("3600");
        assertThat(event.getValorNuevo()).isEqualTo("7200");
    }

    @Test
    @DisplayName("debe_lanzar_404_si_configuracion_no_existe_o_esta_deshabilitada")
    void debe_lanzar_404_si_configuracion_no_existe_o_esta_deshabilitada() {
        // Arrange
        when(configuracionRepository.findByClaveAndEnabledTrue("invalida")).thenReturn(Optional.empty());
        ConfiguracionUpdateRequest request = new ConfiguracionUpdateRequest("test");

        // Act & Assert
        assertThatThrownBy(() -> configuracionService.actualizarConfiguracion("invalida", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Configuración no encontrada o deshabilitada");

        verify(configuracionRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
