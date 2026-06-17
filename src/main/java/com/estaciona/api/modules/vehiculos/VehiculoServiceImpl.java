package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse.PropietarioResumen;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio para el registro de vehículos.
 */
@Service
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoFactory vehiculoFactory;

    public VehiculoServiceImpl(VehiculoRepository vehiculoRepository,
                               UsuarioRepository usuarioRepository,
                               VehiculoFactory vehiculoFactory) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoFactory = vehiculoFactory;
    }

    @Override
    @Transactional
    public VehiculoResponse registrarVehiculo(VehiculoRequest request, UUID usuarioAutenticadoId) {
        // 1. Cargar el propietario (defensa defensiva — el JWT ya lo valida, pero
        //    si el usuario fue eliminado entre la emisión y el uso del token, falla limpio)
        Usuario propietario = usuarioRepository.findById(usuarioAutenticadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioAutenticadoId));

        // 2. Validar unicidad de placa (normalizada a mayúsculas para comparación)
        String placaNormalizada = request.placa().toUpperCase().trim().replace(" ", "");
        if (vehiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw new DuplicateResourceException("Vehículo", "placa", placaNormalizada);
        }

        // 3. Construir Vehiculo vía Factory (normalización + punto de extensión por rol)
        Vehiculo vehiculo = vehiculoFactory.crear(request, propietario);

        // 4. Persistir
        Vehiculo vehiculoPersistido = vehiculoRepository.save(vehiculo);

        // 5. Mapear a VehiculoResponse
        return toResponse(vehiculoPersistido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResumenProjection> consultarMisVehiculos(UUID usuarioId) {
        return vehiculoRepository.findByUsuarioIdAndEnabledTrue(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoBuscadoProjection buscarPorPlaca(String placa) {
        String placaNormalizada = placa.toUpperCase().trim().replace(" ", "");
        return vehiculoRepository.findBuscadoByPlacaIgnoreCase(placaNormalizada)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con placa: " + placa));
    }

    /** Mapeo manual: Vehiculo → VehiculoResponse (sin MapStruct — entidad simple). */
    private VehiculoResponse toResponse(Vehiculo vehiculo) {
        PropietarioResumen propietario = new PropietarioResumen(
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
