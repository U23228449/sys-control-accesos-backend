package com.estaciona.api.modules.configuraciones;

import com.estaciona.api.modules.configuraciones.dto.ConfiguracionResponse;
import com.estaciona.api.modules.configuraciones.dto.ConfiguracionUpdateRequest;

import java.util.List;

/**
 * Contrato del servicio de configuraciones del sistema (HU-017).
 */
public interface ConfiguracionService {

    /** Actualiza el valor de una configuración. Publica un evento post-actualización. */
    ConfiguracionResponse actualizarConfiguracion(String clave, ConfiguracionUpdateRequest request);

    /** Lista todas las configuraciones habilitadas. */
    List<ConfiguracionResponse> listarConfiguraciones();
}
