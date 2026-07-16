package com.estaciona.api.modules.zonas;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.campus.CampusRepository;
import com.estaciona.api.modules.campus.entity.Campus;
import com.estaciona.api.modules.zonas.dto.*;
import com.estaciona.api.modules.zonas.entity.Zona;
import com.estaciona.api.modules.zonas.entity.ZonaEstadoEnum;
import com.estaciona.api.modules.zonas.mapper.ZonaMapper;
import com.estaciona.api.modules.zonas.update.ZonaEstadoCommandFactory;
import com.estaciona.api.modules.zonas.update.ZonaEstadoTransicionStrategy;
import com.estaciona.api.modules.zonas.update.ZonaUpdateValidationStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio para zonas de estacionamiento (HU-011).
 */
@Service
public class ZonaEstacionamientoServiceImpl implements ZonaEstacionamientoService {

    private final ZonaRepository zonaRepository;
    private final CampusRepository campusRepository;
    private final ZonaMapper zonaMapper;
    private final List<ZonaUpdateValidationStrategy> updateStrategies;
    private final ZonaEstadoCommandFactory estadoCommandFactory;
    private final ZonaEstadoTransicionStrategy estadoTransicionStrategy;

    public ZonaEstacionamientoServiceImpl(ZonaRepository zonaRepository,
                                          CampusRepository campusRepository,
                                          ZonaMapper zonaMapper,
                                          List<ZonaUpdateValidationStrategy> updateStrategies,
                                          ZonaEstadoCommandFactory estadoCommandFactory,
                                          ZonaEstadoTransicionStrategy estadoTransicionStrategy) {
        this.zonaRepository = zonaRepository;
        this.campusRepository = campusRepository;
        this.zonaMapper = zonaMapper;
        this.updateStrategies = updateStrategies;
        this.estadoCommandFactory = estadoCommandFactory;
        this.estadoTransicionStrategy = estadoTransicionStrategy;
    }

    @Override
    @Transactional
    public ZonaResponse crearZona(ZonaRequest request) {
        // 1. Validar que el campus existe y está activo
        Campus campus = campusRepository.findById(request.campusId())
                .filter(Campus::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Campus", request.campusId()));

        // 2. Validar unicidad del nombre dentro del campus
        if (zonaRepository.existsByCampusIdAndNombreIgnoreCase(request.campusId(), request.nombre())) {
            throw new DuplicateResourceException(
                    "Zona", "nombre", request.nombre() + " en campus id=" + request.campusId());
        }

        // 3. Construir y persistir
        Zona zona = zonaMapper.toEntity(request, campus);
        Zona zonaPersistida = zonaRepository.save(zona);
        return zonaMapper.toResponse(zonaPersistida);
    }

    @Override
    @Transactional
    public ZonaResponse actualizarZona(Integer id, ZonaUpdateRequest request) {
        // 1. Cargar zona (404 si no existe o está deshabilitada)
        Zona zona = zonaRepository.findById(id)
                .filter(Zona::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Zona", id));

        // 2. Ejecutar estrategias de validación
        for (ZonaUpdateValidationStrategy strategy : updateStrategies) {
            strategy.validar(zona, request, zonaRepository);
        }

        // 3. Recalcular aforoDisponible según el cambio de aforoMaximo
        int diferencia = request.aforoMaximo() - zona.getAforoMaximo();
        int nuevoAforoDisponible = zona.getAforoDisponible() + diferencia;

        // 4. Actualizar campos
        zona.setNombre(request.nombre());
        zona.setUbicacion(request.ubicacion());
        zona.setTipo(request.tipo());
        zona.setAforoMaximo(request.aforoMaximo());
        zona.setAforoDisponible(nuevoAforoDisponible);

        Zona actualizada = zonaRepository.save(zona);
        return zonaMapper.toResponse(actualizada);
    }

    @Override
    @Transactional
    public ZonaResponse actualizarEstado(Integer id, ZonaUpdateEstadoRequest request) {
        // 1. Cargar zona (404 si no existe)
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona", id));

        String nuevoEstado;
        if (request != null && request.estado() != null) {
            nuevoEstado = request.estado();
        } else {
            nuevoEstado = "activa".equalsIgnoreCase(zona.getEstado()) ? "cerrada" : "activa";
        }

        // 2. La factory valida las reglas de negocio y retorna el Runnable que aplica el cambio
        Runnable comando = estadoCommandFactory.crearComando(zona, nuevoEstado);

        // 3. Ejecutar el comando
        comando.run();

        // 4. Log adicional
        estadoTransicionStrategy.aplicar(zona, zona.getEstado());

        Zona actualizada = zonaRepository.save(zona);
        return zonaMapper.toResponse(actualizada);
    }

    private record ZonaResumenProjectionImpl(
            Integer id,
            String campusNombre,
            String nombre,
            String ubicacion,
            String tipo,
            Integer aforoMaximo,
            Integer aforoDisponible,
            String estado,
            Boolean enabled,
            java.time.Instant createdAt
    ) implements ZonaResumenProjection {
        @Override public Integer getId() { return id; }
        @Override public String getCampusNombre() { return campusNombre; }
        @Override public String getNombre() { return nombre; }
        @Override public String getUbicacion() { return ubicacion; }
        @Override public String getTipo() { return tipo; }
        @Override public Integer getAforoMaximo() { return aforoMaximo; }
        @Override public Integer getAforoDisponible() { return aforoDisponible; }
        @Override public String getEstado() { return estado; }
        @Override public Boolean getEnabled() { return enabled; }
        @Override public java.time.Instant getCreatedAt() { return createdAt; }
    }

    private record ZonaEstacionamientoResumenProjectionImpl(
            Integer id,
            String nombre,
            Integer aforoMaximo,
            Integer aforoDisponible,
            String estado
    ) implements ZonaEstacionamientoResumenProjection {
        @Override public Integer getId() { return id; }
        @Override public String getNombre() { return nombre; }
        @Override public Integer getAforoMaximo() { return aforoMaximo; }
        @Override public Integer getAforoDisponible() { return aforoDisponible; }
        @Override public String getEstado() { return estado; }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ZonaResumenProjection> consultarZonas(ZonaFiltroRequest filtro, Pageable pageable) {
        if (filtro != null && filtro.estado() != null && !filtro.estado().isBlank()) {
            try {
                ZonaEstadoEnum.valueOf(filtro.estado().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado '" + filtro.estado() + "' no es un estado de zona válido.");
            }
        }
        Specification<Zona> spec = com.estaciona.api.modules.zonas.spec.ZonaSpecifications.construir(filtro);
        Page<Zona> zonasPage = zonaRepository.findAll(spec, pageable);
        if (zonasPage.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron zonas para los criterios aplicados.");
        }
        return zonasPage.map(zona -> new ZonaResumenProjectionImpl(
                zona.getId(),
                zona.getCampus().getNombre(),
                zona.getNombre(),
                zona.getUbicacion(),
                zona.getTipo(),
                zona.getAforoMaximo(),
                zona.getAforoDisponible(),
                zona.getEstado(),
                zona.isEnabled(),
                zona.getCreatedAt()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZonaResumenProjection> consultarZonas(ZonaFiltroRequest filtro) {
        if (filtro != null && filtro.estado() != null && !filtro.estado().isBlank()) {
            try {
                ZonaEstadoEnum.valueOf(filtro.estado().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado '" + filtro.estado() + "' no es un estado de zona válido.");
            }
        }
        Specification<Zona> spec = com.estaciona.api.modules.zonas.spec.ZonaSpecifications.construir(filtro);
        List<Zona> zonasList = zonaRepository.findAll(spec);
        if (zonasList.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron zonas para los criterios aplicados.");
        }
        return zonasList.stream().map(zona -> new ZonaResumenProjectionImpl(
                zona.getId(),
                zona.getCampus().getNombre(),
                zona.getNombre(),
                zona.getUbicacion(),
                zona.getTipo(),
                zona.getAforoMaximo(),
                zona.getAforoDisponible(),
                zona.getEstado(),
                zona.isEnabled(),
                zona.getCreatedAt()
        )).map(impl -> (ZonaResumenProjection) impl).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ZonaEstacionamientoResumenProjection> consultarZonasEstacionamiento(ZonaFiltroRequest filtro, Pageable pageable) {
        if (filtro != null && filtro.estado() != null && !filtro.estado().isBlank()) {
            try {
                ZonaEstadoEnum.valueOf(filtro.estado().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El estado '" + filtro.estado() + "' no es un estado de zona válido.");
            }
        }
        Specification<Zona> spec = com.estaciona.api.modules.zonas.spec.ZonaSpecifications.construir(filtro);
        Page<Zona> zonasPage = zonaRepository.findAll(spec, pageable);
        if (zonasPage.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron zonas de estacionamiento para los criterios aplicados.");
        }
        return zonasPage.map(zona -> new ZonaEstacionamientoResumenProjectionImpl(
                zona.getId(),
                zona.getNombre(),
                zona.getAforoMaximo(),
                zona.getAforoDisponible(),
                zona.getEstado()
        ));
    }
}
