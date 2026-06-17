package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.campus.CampusRepository;
import com.estaciona.api.modules.campus.entity.Campus;
import com.estaciona.api.modules.zonas.dto.ZonaRequest;
import com.estaciona.api.modules.zonas.dto.ZonaResponse;
import com.estaciona.api.modules.zonas.entity.Zona;
import com.estaciona.api.modules.zonas.mapper.ZonaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de negocio para zonas de estacionamiento.
 */
@Service
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;
    private final CampusRepository campusRepository;
    private final ZonaMapper zonaMapper;

    public ZonaServiceImpl(ZonaRepository zonaRepository,
                           CampusRepository campusRepository,
                           ZonaMapper zonaMapper) {
        this.zonaRepository = zonaRepository;
        this.campusRepository = campusRepository;
        this.zonaMapper = zonaMapper;
    }

    @Override
    @Transactional
    public ZonaResponse crearZona(ZonaRequest request) {
        // 1. Validar que el campus existe y está activo
        Campus campus = campusRepository.findById(request.campusId())
                .filter(Campus::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campus", request.campusId()));

        // 2. Validar unicidad del nombre dentro del campus
        if (zonaRepository.existsByCampusIdAndNombreIgnoreCase(request.campusId(), request.nombre())) {
            throw new DuplicateResourceException(
                    "Zona", "nombre", request.nombre() + " en campus id=" + request.campusId());
        }

        // 3. Construir Zona via Builder a través del mapper
        //    aforoDisponible = aforoMaximo (zona nace con todos sus espacios libres)
        //    estado = "activa" y enabled = true por @Builder.Default
        Zona zona = zonaMapper.toEntity(request, campus);

        // 4. Persistir (createdAt lo asigna @EnableJpaAuditing)
        Zona zonaPersistida = zonaRepository.save(zona);

        // 5. Mapear y retornar respuesta
        return zonaMapper.toResponse(zonaPersistida);
    }
}
