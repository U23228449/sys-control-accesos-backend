package com.estaciona.api.modules.usuarios.eliminacion;

import com.estaciona.api.common.exception.ConflictException;
import com.estaciona.api.modules.accesos.AccesoVehicularRepository;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida que el usuario no tenga un acceso activo ("en_curso") antes de ser eliminado lógicamente.
 */
@Component
public class UsuarioSinAccesoActivoEliminacionStrategy implements UsuarioEliminacionValidationStrategy {

    private final AccesoVehicularRepository accesoRepository;

    public UsuarioSinAccesoActivoEliminacionStrategy(AccesoVehicularRepository accesoRepository) {
        this.accesoRepository = accesoRepository;
    }

    @Override
    public void validar(Usuario usuario, UUID adminId, UsuarioRepository repo) {
        boolean tieneAccesoActivo = accesoRepository.existsByUsuarioIdAndEstado(usuario.getId(), "en_curso");
        if (tieneAccesoActivo) {
            throw new ConflictException(
                    "El usuario tiene un acceso vehicular en curso y no puede ser eliminado.");
        }
    }
}
