package com.estaciona.api.modules.accesos;

import com.estaciona.api.modules.accesos.dto.AccesoVehicularFiltroRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularHistorialProjection;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularRequest;
import com.estaciona.api.modules.accesos.dto.AccesoVehicularResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato para el servicio de acceso vehicular (ingreso, salida e historial).
 */
public interface AccesoVehicularService {

    /**
     * Registra el ingreso de un vehículo.
     */
    AccesoVehicularResponse registrarIngreso(AccesoVehicularRequest request, UUID guardiaId);

    /**
     * Registra la salida de un vehículo.
     */
    AccesoVehicularResponse registrarSalida(UUID accesoId, UUID guardiaSalidaId);

    /**
     * Consulta el historial paginado de accesos vehiculares con filtros opcionales. (HU-015)
     */
    Page<AccesoVehicularHistorialProjection> consultarHistorial(AccesoVehicularFiltroRequest filtro, Pageable pageable);

    /**
     * Obtiene la lista de todos los accesos vehiculares activos (en curso).
     */
    java.util.List<AccesoVehicularResponse> obtenerAccesosActivos();
}
