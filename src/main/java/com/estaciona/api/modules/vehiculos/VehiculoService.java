package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.modules.vehiculos.dto.VehiculoBuscadoProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponse;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResumenProjection;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequest;
import com.estaciona.api.modules.vehiculos.dto.VehiculoUpdateRequestDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoResponseDTO;
import com.estaciona.api.modules.vehiculos.dto.VehiculoDesvinculadoResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * Contrato del servicio de vehículos.
 */
public interface VehiculoService {

    VehiculoResponse registrarVehiculo(VehiculoRequest request, UUID usuarioAutenticadoId);

    /** Devuelve la lista de vehículos habilitados del usuario autenticado. */
    List<VehiculoResumenProjection> consultarMisVehiculos(UUID usuarioId);

    /** Busca un vehículo por su placa. Lanza ResourceNotFoundException si no se encuentra. */
    VehiculoBuscadoProjection buscarPorPlaca(String placa);

    /** Actualiza tipo, marcaModelo y color de un vehículo. La placa es inmutable. (HU-008) */
    VehiculoResponseDTO actualizarVehiculo(UUID id, UUID usuarioId, VehiculoUpdateRequestDTO request);

    /** Elimina (soft delete) un vehículo. Solo el propietario puede hacerlo. (HU-009) */
    VehiculoDesvinculadoResponseDTO eliminarVehiculo(UUID id, UUID usuarioId);

    /** Reactiva (habilita) un vehículo inactivo. Solo el propietario puede hacerlo. */
    VehiculoResponseDTO reactivarVehiculo(UUID id, UUID usuarioId);
}
