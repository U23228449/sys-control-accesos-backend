package com.estaciona.api.modules.usuarios.eliminacion;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Estrategia que protege al único administrador activo de ser eliminado.
 * Si el usuario a eliminar tiene rol ADMINISTRADOR y es el único activo, lanza excepción.
 */
@Component
public class UltimoAdminProteccionEliminacionStrategy implements UsuarioEliminacionValidationStrategy {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Override
    public void validar(Usuario usuario, UUID adminId, UsuarioRepository repo) {
        if (ROL_ADMINISTRADOR.equals(usuario.getRol().getNombre())) {
            long totalAdminsActivos = repo.countByRolNombreAndEnabledTrue(ROL_ADMINISTRADOR);
            if (totalAdminsActivos <= 1) {
                throw new BusinessRuleException(
                        "No se puede eliminar al único administrador activo del sistema.");
            }
        }
    }
}
