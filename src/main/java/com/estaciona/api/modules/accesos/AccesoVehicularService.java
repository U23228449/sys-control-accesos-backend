package com.estaciona.api.modules.accesos;

import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;

import java.util.UUID;

/**
 * Contrato para el servicio de acceso vehicular (ingreso y salida).
 */
public interface AccesoVehicularService {

    /**
     * Registra el ingreso de un vehículo.
     *
     * @param request   DTO con los datos de ingreso (placa, zona).
     * @param guardiaId UUID del guardia que registra el ingreso.
     * @return DTO con los detalles del acceso registrado.
     */
    AccesoVehicularResponse registrarIngreso(AccesoVehicularRequest request, UUID guardiaId);

    /**
     * Registra la salida de un vehículo.
     *
     * @param accesoId         UUID del acceso vehicular a cerrar.
     * @param guardiaSalidaId  UUID del guardia que registra la salida.
     * @return DTO con los detalles del acceso actualizado.
     */
    AccesoVehicularResponse registrarSalida(UUID accesoId, UUID guardiaSalidaId);
}
