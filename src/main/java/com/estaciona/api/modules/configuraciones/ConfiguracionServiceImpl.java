package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionUpdateRequest;
import com.estaciona.api.modules.configuraciones.entity.Configuracion;
import com.estaciona.api.modules.configuraciones.event.ConfiguracionCambiadaEvent;
import com.estaciona.api.modules.configuraciones.strategy.ConfiguracionValidationStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio para la gestión de configuraciones del sistema.
 */
@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;
    private final ConfiguracionHandlerFactory handlerFactory;
    private final ApplicationEventPublisher eventPublisher;

    public ConfiguracionServiceImpl(ConfiguracionRepository configuracionRepository,
                                    ConfiguracionHandlerFactory handlerFactory,
                                    ApplicationEventPublisher eventPublisher) {
        this.configuracionRepository = configuracionRepository;
        this.handlerFactory = handlerFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ConfiguracionResponse actualizarConfiguracion(String clave, ConfiguracionUpdateRequest request) {
        // 1. Cargar configuración (404 si no existe o está deshabilitada)
        Configuracion config = configuracionRepository.findByClaveAndEnabledTrue(clave)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada o deshabilitada: " + clave));

        // 2. Resolver strategy de validación por Factory Method
        ConfiguracionValidationStrategy strategy = handlerFactory.resolver(clave);

        // 3. Validar el nuevo valor (lanza BusinessRuleException si es inválido)
        strategy.validar(clave, request.valor());

        // 4. Guardar valor anterior para el evento
        String valorAnterior = config.getValor();

        // 5. Actualizar y persistir
        config.setValor(request.valor());
        Configuracion actualizada = configuracionRepository.save(config);

        // 6. Publicar evento (Observer Pattern) para notificar a otros componentes
        eventPublisher.publishEvent(
                new ConfiguracionCambiadaEvent(this, clave, valorAnterior, request.valor()));

        return toResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> listarConfiguraciones() {
        return configuracionRepository.findAll().stream()
                .filter(Configuracion::isEnabled)
                .map(this::toResponse)
                .toList();
    }

    private ConfiguracionResponse toResponse(Configuracion config) {
        return new ConfiguracionResponse(
                config.getId(),
                config.getClave(),
                config.getValor(),
                config.getDescripcion(),
                config.isEnabled()
        );
    }
}
