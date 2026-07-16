package com.estaciona.api.modules.zonas.update;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.dto.ZonaUpdateRequest;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Estrategia que valida que el nuevo aforo máximo sea coherente con los vehículos
 * actualmente dentro de la zona (espacios ocupados = aforoMaximo - aforoDisponible actual).
 */
@Component
public class AforoCoherenteUpdateStrategy implements ZonaUpdateValidationStrategy {

    @Override
    public void validar(Zona zonaActual, ZonaUpdateRequest request, ZonaRepository zonaRepository) {
        // Calcular cuántos espacios están actualmente ocupados
        int espaciosOcupados = zonaActual.getAforoMaximo() - zonaActual.getAforoDisponible();

        // El nuevo aforo no puede ser menor que los vehículos actualmente dentro
        if (request.aforoMaximo() < espaciosOcupados) {
            throw new BusinessRuleException(
                    "El nuevo aforo es inferior a los vehículos actualmente en la zona. " +
                    "Espacios ocupados actualmente: " + espaciosOcupados + ".");
        }
    }
}
