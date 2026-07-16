package com.estaciona.api.modules.configuraciones.event;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando una configuración del sistema es actualizada (HU-017).
 * Permite que otros componentes (ej: JwtTokenProvider) reaccionen al cambio en tiempo real.
 */
public class ConfiguracionCambiadaEvent extends ApplicationEvent {

    private final String clave;
    private final String valorAnterior;
    private final String valorNuevo;

    public ConfiguracionCambiadaEvent(Object source, String clave, String valorAnterior, String valorNuevo) {
        super(source);
        this.clave = clave;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
    }

    public String getClave() { return clave; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
}
