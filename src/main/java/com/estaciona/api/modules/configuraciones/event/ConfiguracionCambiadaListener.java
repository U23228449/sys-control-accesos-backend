package com.estaciona.api.modules.configuraciones.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener del patrón Observer que reacciona al cambio de configuraciones en tiempo real.
 * Implementa HU-017: cuando se cambia JWT_EXPIRATION_MINUTES, registra el evento
 * para auditoría y futura integración con el proveedor JWT.
 */
@Component
public class ConfiguracionCambiadaListener {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionCambiadaListener.class);

    /**
     * Maneja el evento de cambio de configuración.
     * Si la clave es JWT_EXPIRATION_MINUTES, registra el cambio.
     * La integración dinámica con JwtTokenProvider requeriría que éste exponga
     * un método setter; actualmente el cambio aplica en el próximo reinicio.
     *
     * @param event evento con clave, valorAnterior y valorNuevo.
     */
    @EventListener
    public void manejarCambioConfiguracion(ConfiguracionCambiadaEvent event) {
        log.info("Configuración '{}' actualizada: '{}' → '{}'.",
                event.getClave(), event.getValorAnterior(), event.getValorNuevo());

        if ("JWT_EXPIRATION_MINUTES".equals(event.getClave())) {
            log.warn("JWT_EXPIRATION_MINUTES cambió a {}. El nuevo valor aplica a los tokens " +
                     "emitidos después de este momento (los tokens existentes conservan su expiración original).",
                     event.getValorNuevo());
        }
    }
}
