package com.estaciona.api.modules.zonas.update;

import com.estaciona.api.modules.zonas.entity.Zona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Strategy que aplica una transición de estado validada sobre una Zona.
 * El aforo disponible NO se resetea al cerrar — se conserva para trazabilidad.
 */
@Component
public class ZonaEstadoTransicionStrategy {

    private static final Logger log = LoggerFactory.getLogger(ZonaEstadoTransicionStrategy.class);

    /**
     * Aplica el cambio de estado sobre la zona.
     * Si la zona pasa a "cerrada", el aforo disponible se mantiene sin cambios
     * (los vehículos que salgan luego actualizarán el contador por el flujo de accesos).
     *
     * @param zona        entidad zona a modificar.
     * @param nuevoEstado estado destino (ya validado por ZonaEstadoCommandFactory).
     */
    public void aplicar(Zona zona, String nuevoEstado) {
        String estadoAnterior = zona.getEstado();
        zona.setEstado(nuevoEstado);

        // Nota: el aforo disponible se mantiene sin resetear al cerrar la zona.
        // Los accesos de salida pendientes seguirán actualizando el aforo correctamente.
        if ("cerrada".equals(nuevoEstado)) {
            log.info("Zona id={} cambiada a 'cerrada'. Aforo disponible conservado: {}.",
                    zona.getId(), zona.getAforoDisponible());
        } else {
            log.info("Zona id={} cambiada de '{}' a '{}'.", zona.getId(), estadoAnterior, nuevoEstado);
        }
    }
}
