package com.estaciona.api.modules.usuarios.update;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Factory Method que construye los comandos de actualización administrativa de usuarios.
 * Encapsula las validaciones de reglas de negocio previas a la mutación de la entidad.
 */
@Component
public class UsuarioUpdateCommandFactory {

    private final UsuarioRepository usuarioRepository;

    public UsuarioUpdateCommandFactory(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea el comando para cambiar el rol de un usuario.
     * Valida que el nuevo rol no sea el mismo que el rol actual.
     *
     * @param usuario  entidad usuario a modificar.
     * @param rolNuevo nuevo rol a asignar (ya validado como enabled en el Service).
     * @return comando que asigna el nuevo rol sobre la entidad.
     * @throws BusinessRuleException (422) si el rol nuevo es igual al rol actual.
     */
    public UsuarioUpdateCommand crearComandoRol(Usuario usuario, Rol rolNuevo) {
        if (usuario.getRol().getId().equals(rolNuevo.getId())) {
            throw new BusinessRuleException("El usuario ya tiene el rol indicado. No se realizó ningún cambio.");
        }
        return u -> u.setRol(rolNuevo);
    }

    /**
     * Crea el comando para cambiar el estado (enabled) de un usuario.
     * Valida que no se desactive al único administrador activo del sistema.
     *
     * @param usuario     entidad usuario a modificar.
     * @param nuevoEstado nuevo valor de enabled.
     * @param adminId     UUID del administrador que realiza la operación (no usado actualmente, reservado).
     * @return comando que asigna el nuevo estado sobre la entidad.
     * @throws BusinessRuleException (422) si el estado ya es el mismo o si es el último admin activo.
     */
    public UsuarioUpdateCommand crearComandoEstado(Usuario usuario, boolean nuevoEstado, UUID adminId) {
        // Validar que el estado no sea el mismo
        if (usuario.isEnabled() == nuevoEstado) {
            String estadoActual = nuevoEstado ? "habilitado" : "deshabilitado";
            throw new BusinessRuleException("El usuario ya se encuentra " + estadoActual + ". No se realizó ningún cambio.");
        }

        // Si se va a deshabilitar, verificar que no sea el último ADMINISTRADOR activo
        if (!nuevoEstado && "ADMINISTRADOR".equals(usuario.getRol().getNombre())) {
            long adminsActivos = usuarioRepository.countByRolNombreAndEnabledTrue("ADMINISTRADOR");
            if (adminsActivos <= 1) {
                throw new BusinessRuleException(
                        "No se puede deshabilitar al único administrador activo del sistema.");
            }
        }

        return u -> u.setEnabled(nuevoEstado);
    }
}
