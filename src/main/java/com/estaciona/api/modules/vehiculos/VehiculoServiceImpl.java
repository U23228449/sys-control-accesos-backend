package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse.PropietarioResumen;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponseDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoDesvinculadoResponseDTO;
import com.estaciona.api.modules.vehiculos.strategy.VehiculoUpdateValidationStrategy;
import com.estaciona.api.modules.vehiculos.eliminacion.VehiculoEliminacionValidationStrategy;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio para el registro y gestión de vehículos.
 */
@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoFactory vehiculoFactory;
    private final AccesoVehicularRepository accesoRepository;
    private final List<VehiculoEliminacionValidationStrategy> eliminacionStrategies;
    private final List<VehiculoUpdateValidationStrategy> updateStrategies;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository,
                               UsuarioRepository usuarioRepository,
                               VehiculoFactory vehiculoFactory,
                               AccesoVehicularRepository accesoRepository,
                               List<VehiculoEliminacionValidationStrategy> eliminacionStrategies,
                               List<VehiculoUpdateValidationStrategy> updateStrategies) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoFactory = vehiculoFactory;
        this.accesoRepository = accesoRepository;
        this.eliminacionStrategies = eliminacionStrategies;
        this.updateStrategies = updateStrategies;
    }

    @Override
    @Transactional
    public VehiculoResponse registrarVehiculo(VehiculoRequest request, UUID usuarioAutenticadoId) {
        // 1. Cargar el propietario
        Usuario propietario = usuarioRepository.findById(usuarioAutenticadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioAutenticadoId));

        // 2. Validar unicidad de placa o reactivar si existe como inactivo
        String placaNormalizada = request.placa().toUpperCase().trim().replace(" ", "");
        if (vehiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            Vehiculo v = vehiculoRepository.findByPlacaIgnoreCase(placaNormalizada).orElse(null);
            if (v != null && !v.isEnabled()) {
                v.setEnabled(true);
                v.setUsuario(propietario);
                v.setTipo(request.tipo());
                v.setMarcaModelo(request.marcaModelo());
                v.setColor(request.color());
                Vehiculo guardado = vehiculoRepository.save(v);
                return toResponse(guardado);
            } else {
                throw new DuplicateResourceException("Vehículo", "placa", placaNormalizada);
            }
        }

        // 3. Construir vía Factory y persistir
        Vehiculo vehiculo = vehiculoFactory.crear(request, propietario);
        Vehiculo vehiculoPersistido = vehiculoRepository.save(vehiculo);
        return toResponse(vehiculoPersistido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResumenProjection> consultarMisVehiculos(UUID usuarioId) {
        return vehiculoRepository.findAllResumenByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoBuscadoProjection buscarPorPlaca(String placa) {
        String placaNormalizada = placa.toUpperCase().trim().replace(" ", "");
        return vehiculoRepository.findBuscadoByPlacaIgnoreCase(placaNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con placa: " + placa));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizarVehiculo(UUID id, UUID usuarioId, VehiculoUpdateRequestDTO request) {
        // 1. Cargar vehículo (404 si no existe)
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        // 2. Validar que el usuario sea el propietario y que el vehículo esté habilitado
        for (VehiculoEliminacionValidationStrategy strategy : eliminacionStrategies) {
            if (!(strategy instanceof com.estaciona.api.modules.vehiculos.eliminacion.VehiculoSinAccesoActivoEliminacionStrategy)) {
                strategy.validar(vehiculo, usuarioId, accesoRepository);
            }
        }

        // 3. Ejecutar estrategias de validación de campos
        for (VehiculoUpdateValidationStrategy strategy : updateStrategies) {
            strategy.validar(request);
        }

        // 4. Reconstruir usando patrón Builder
        Vehiculo actualizado = vehiculo.toBuilder()
                .tipo(request.tipo())
                .marcaModelo(request.marcaModelo())
                .color(request.color())
                .build();

        Vehiculo guardado = vehiculoRepository.save(actualizado);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public VehiculoDesvinculadoResponseDTO eliminarVehiculo(UUID id, UUID usuarioId) {
        // 1. Cargar vehículo (404 si no existe)
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        // 2. Ejecutar todas las estrategias de validación
        for (VehiculoEliminacionValidationStrategy strategy : eliminacionStrategies) {
            strategy.validar(vehiculo, usuarioId, accesoRepository);
        }

        // 3. Soft delete
        vehiculo.setEnabled(false);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        return new VehiculoDesvinculadoResponseDTO(
                "Vehículo desvinculado correctamente.",
                guardado.getId(),
                guardado.getPlaca(),
                guardado.getMarcaModelo(),
                guardado.getColor(),
                guardado.getUsuario().getNombreCompleto()
        );
    }

    @Override
    @Transactional
    public VehiculoResponseDTO reactivarVehiculo(UUID id, UUID usuarioId) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));

        if (!vehiculo.getUsuario().getId().equals(usuarioId)) {
            throw new com.estaciona.api.common.exception.ForbiddenOperationException("No tiene permisos para reactivar este vehículo.");
        }

        vehiculo.setEnabled(true);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return toResponseDTO(guardado);
    }

    private VehiculoResponseDTO toResponseDTO(Vehiculo vehiculo) {
        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getTipo(),
                vehiculo.getPlaca(),
                vehiculo.getMarcaModelo(),
                vehiculo.getColor(),
                vehiculo.isEnabled(),
                new VehiculoResponseDTO.PropietarioResumen(
                        vehiculo.getUsuario().getId(),
                        vehiculo.getUsuario().getNombreCompleto()
                )
        );
    }

    private VehiculoResponse toResponse(Vehiculo vehiculo) {
        VehiculoResponse.PropietarioResumen propietario = new VehiculoResponse.PropietarioResumen(
                vehiculo.getUsuario().getId(),
                vehiculo.getUsuario().getNombreCompleto()
        );
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getTipo(),
                vehiculo.getPlaca(),
                vehiculo.getMarcaModelo(),
                vehiculo.getColor(),
                vehiculo.isEnabled(),
                propietario
        );
    }
}
