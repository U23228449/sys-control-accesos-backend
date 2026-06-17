package com.estaciona.api.modules.accesos;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;
import com.estaciona.api.modules.accesos.entity.AccesoVehicular;
import com.estaciona.api.modules.accesos.validation.AccesoVehicularValidationStrategy;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.VehiculoRepository;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import com.estaciona.api.modules.zonas.ZonaRepository;
import com.estaciona.api.modules.zonas.entity.Zona;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de acceso vehicular.
 */
@Service
public class AccesoVehicularServiceImpl implements AccesoVehicularService {

    private final AccesoVehicularRepository accesoVehicularRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ZonaRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;
    private final List<AccesoVehicularValidationStrategy> validationStrategies;

    public AccesoVehicularServiceImpl(AccesoVehicularRepository accesoVehicularRepository,
                                      VehiculoRepository vehiculoRepository,
                                      ZonaRepository zonaRepository,
                                      UsuarioRepository usuarioRepository,
                                      List<AccesoVehicularValidationStrategy> validationStrategies) {
        this.accesoVehicularRepository = accesoVehicularRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.zonaRepository = zonaRepository;
        this.usuarioRepository = usuarioRepository;
        this.validationStrategies = validationStrategies;
    }

    @Override
    @Transactional
    public AccesoVehicularResponse registrarIngreso(AccesoVehicularRequest request, UUID guardiaId) {
        // 1. Buscar vehículo por placa (normalizado a mayúsculas)
        String placaNormalizada = request.placa().toUpperCase().trim().replace(" ", "");
        Vehiculo vehiculo = vehiculoRepository.findByPlacaIgnoreCase(placaNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Placa no registrada: " + request.placa()));

        // 2. Buscar zona de estacionamiento
        Zona zona = zonaRepository.findById(request.zonaId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona de estacionamiento", request.zonaId()));

        // 3. Ejecutar todas las estrategias de validación
        for (AccesoVehicularValidationStrategy strategy : validationStrategies) {
            strategy.validar(vehiculo, zona);
        }

        // 4. Buscar guardia de entrada
        Usuario guardiaEntrada = usuarioRepository.findById(guardiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", guardiaId));

        // 5. Construir el acceso vehicular
        AccesoVehicular acceso = AccesoVehicular.builder()
                .usuario(vehiculo.getUsuario()) // propietario
                .vehiculo(vehiculo)
                .zona(zona)
                .guardiaEntrada(guardiaEntrada)
                .horaIngreso(OffsetDateTime.now())
                .estado("en_curso")
                .enabled(true)
                .build();

        // 6. Decrementar el aforo disponible de la zona
        zona.setAforoDisponible(zona.getAforoDisponible() - 1);
        zonaRepository.save(zona);

        // 7. Persistir acceso
        AccesoVehicular accesoPersistido = accesoVehicularRepository.save(acceso);

        return toResponse(accesoPersistido);
    }

    @Override
    @Transactional
    public AccesoVehicularResponse registrarSalida(UUID accesoId, UUID guardiaSalidaId) {
        // 1. Buscar el acceso vehicular
        AccesoVehicular acceso = accesoVehicularRepository.findById(accesoId)
                .orElseThrow(() -> new ResourceNotFoundException("Acceso vehicular", accesoId));

        // 2. Validar que el acceso esté en curso
        if (!"en_curso".equalsIgnoreCase(acceso.getEstado()) || acceso.getHoraSalida() != null) {
            throw new DuplicateResourceException("El acceso vehicular ya se encuentra completado.");
        }

        // 3. Buscar guardia de salida
        Usuario guardiaSalida = usuarioRepository.findById(guardiaSalidaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", guardiaSalidaId));

        // 4. Completar acceso
        acceso.setHoraSalida(OffsetDateTime.now());
        acceso.setGuardiaSalida(guardiaSalida);
        acceso.setEstado("completada");

        // 5. Incrementar el aforo disponible de la zona
        Zona zona = acceso.getZona();
        zona.setAforoDisponible(zona.getAforoDisponible() + 1);
        zonaRepository.save(zona);

        // 6. Guardar cambios en el acceso
        AccesoVehicular accesoActualizado = accesoVehicularRepository.save(acceso);

        return toResponse(accesoActualizado);
    }

    /** Mapeo manual de AccesoVehicular a AccesoVehicularResponse. */
    private AccesoVehicularResponse toResponse(AccesoVehicular acceso) {
        return new AccesoVehicularResponse(
                acceso.getId(),
                acceso.getVehiculo().getPlaca(),
                acceso.getVehiculo().getMarcaModelo(),
                acceso.getUsuario().getNombreCompleto(),
                acceso.getZona().getNombre(),
                acceso.getGuardiaEntrada().getNombreCompleto(),
                acceso.getGuardiaSalida() != null ? acceso.getGuardiaSalida().getNombreCompleto() : null,
                acceso.getHoraIngreso(),
                acceso.getHoraSalida(),
                acceso.getEstado()
        );
    }
}
