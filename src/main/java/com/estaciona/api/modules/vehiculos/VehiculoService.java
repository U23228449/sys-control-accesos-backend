package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection;

import java.util.List;
import java.util.UUID;

/**
 * Contrato del servicio de vehículos.
 */
public interface VehiculoService {

    VehiculoResponse registrarVehiculo(VehiculoRequest request, UUID usuarioAutenticadoId);

    /**
     * Devuelve la lista de vehículos habilitados del usuario autenticado.
     */
    List<VehiculoResumenProjection> consultarMisVehiculos(UUID usuarioId);

    /**
     * Busca un vehículo por su placa. Lanza ResourceNotFoundException si no se encuentra.
     */
    VehiculoBuscadoProjection buscarPorPlaca(String placa);
}
