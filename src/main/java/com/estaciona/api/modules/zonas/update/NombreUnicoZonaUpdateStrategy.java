package com.estaciona.api.modules.zonas.update;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.dto.ZonaUpdateRequest;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Component;

/**
 * Estrategia que valida que el nuevo nombre no esté en uso por otra zona del mismo campus.
 */
@Component
public class NombreUnicoZonaUpdateStrategy implements ZonaUpdateValidationStrategy {

    @Override
    public void validar(Zona zonaActual, ZonaUpdateRequest request, ZonaRepository zonaRepository) {
        boolean nombreDuplicado = zonaRepository.existsByCampusIdAndNombreIgnoreCaseAndIdNot(
                zonaActual.getCampus().getId(),
                request.nombre(),
                zonaActual.getId()
        );
        if (nombreDuplicado) {
            throw new DuplicateResourceException(
                    "Zona", "nombre", request.nombre() + " en campus id=" + zonaActual.getCampus().getId());
        }
    }
}
