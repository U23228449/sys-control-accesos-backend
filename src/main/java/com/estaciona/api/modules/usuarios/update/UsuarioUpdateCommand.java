package com.estaciona.api.modules.usuarios.update;

import com.estaciona.api.modules.usuarios.entity.Usuario;

/**
 * Interfaz funcional que encapsula un comando de actualización sobre un Usuario.
 * Aplica el patrón Command, permitiendo construir la mutación a ejecutar en el Service.
 */
@FunctionalInterface
public interface UsuarioUpdateCommand {

    /**
     * Aplica la modificación sobre la entidad usuario.
     *
     * @param usuario entidad a mutar en memoria (luego persistida por el Service).
     */
    void apply(Usuario usuario);
}
