package com.estaciona.api.modules.vehiculos;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.vehiculos.dto.VehiculoRequest;
import com.estaciona.api.modules.vehiculos.entity.Vehiculo;
import org.springframework.stereotype.Component;

/**
 * Factory Method para construir entidades Vehiculo.
 *
 * Patrón Factory Method — HU-006:
 *  - Centraliza la creación y normalización de Vehiculo.
 *  - El switch sobre el ROL del propietario es el punto de extensión
 *    para futuras HUs donde diferentes roles puedan tener reglas distintas
 *    (ej. límite de vehículos, tipos permitidos, validaciones extra).
 *  - Hoy todos los roles comparten la misma lógica base.
 */
@Component
public class VehiculoFactory {

    /**
     * Crea un Vehiculo normalizado según el rol del propietario.
     *
     * @param request     datos de entrada validados por Bean Validation.
     * @param propietario usuario autenticado resuelto desde el JWT.
     * @return Vehiculo listo para persistir (no tiene ID aún).
     */
    public Vehiculo crear(VehiculoRequest request, Usuario propietario) {
        // Punto de extensión: comportamiento por rol (listo para futuras HUs)
        return switch (propietario.getRol().getNombre()) {
            case "ADMINISTRADOR", "COORDINADOR_SEGURIDAD" -> construirVehiculo(request, propietario);
            case "SEGURIDAD", "USUARIO"                   -> construirVehiculo(request, propietario);
            default                                        -> construirVehiculo(request, propietario);
        };
    }

    /**
     * Construcción base compartida por todos los roles.
     * Normaliza la placa a MAYÚSCULAS sin espacios y el tipo a minúsculas.
     */
    private Vehiculo construirVehiculo(VehiculoRequest request, Usuario propietario) {
        String placaNormalizada = request.placa().toUpperCase().trim().replace(" ", "");
        String tipoNormalizado  = request.tipo().toLowerCase().trim();

        return Vehiculo.builder()
                .usuario(propietario)
                .tipo(tipoNormalizado)
                .placa(placaNormalizada)
                .marcaModelo(request.marcaModelo().trim())
                .color(request.color().trim())
                .enabled(true)
                .build();
    }
}
