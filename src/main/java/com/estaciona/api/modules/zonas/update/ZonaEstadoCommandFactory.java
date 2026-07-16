package com.estaciona.api.modules.zonas.update;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Factory Method que construye el comando de cambio de estado de una zona,
 * validando que la transición sea permitida según las reglas de negocio.
 */
@Component
public class ZonaEstadoCommandFactory {

    /**
     * Valida la transición de estado y retorna un Runnable que aplica el cambio.
     *
     * <ul>
     *   <li>activa → cerrada: solo si no hay vehículos dentro (aforoDisponible == aforoMaximo).</li>
     *   <li>cerrada → activa: siempre permitido.</li>
     *   <li>mismo estado: BusinessRuleException (422).</li>
     * </ul>
     *
     * @param zona        entidad zona actual.
     * @param nuevoEstado estado destino ("activa" o "cerrada").
     * @return Runnable que, al ejecutarse, aplica la transición sobre la entidad.
     * @throws BusinessRuleException (422) si la transición no es válida.
     */
    public Runnable crearComando(Zona zona, String nuevoEstado) {
        // Validar que no sea el mismo estado
        if (nuevoEstado.equals(zona.getEstado())) {
            throw new BusinessRuleException(
                    "La zona ya tiene el estado '" + nuevoEstado + "'. No se realizó ningún cambio.");
        }

        // Validar cierre con vehículos dentro
        if ("cerrada".equals(nuevoEstado)) {
            boolean hayVehiculosDentro = zona.getAforoDisponible() < zona.getAforoMaximo();
            if (hayVehiculosDentro) {
                throw new BusinessRuleException(
                        "No se puede cerrar la zona con vehículos dentro. " +
                        "Espacios ocupados: " + (zona.getAforoMaximo() - zona.getAforoDisponible()) + ".");
            }
        }

        // La transición cerrada → activa siempre está permitida
        return () -> zona.setEstado(nuevoEstado);
    }
}
