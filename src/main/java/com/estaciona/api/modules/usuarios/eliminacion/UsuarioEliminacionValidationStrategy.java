package com.estaciona.api.modules.usuarios.eliminacion;

import com.estaciona.api.modules.usuarios.UsuarioRepository;
import com.estaciona.api.modules.usuarios.entity.Usuario;

import java.util.UUID;

/**
 * Contrato para las estrategias de validación antes de eliminar un usuario (soft delete).
 * Cada implementación valida una regla de negocio específica.
 */
public interface UsuarioEliminacionValidationStrategy {

    /**
     * Ejecuta la validación. Lanza una excepción si la regla no se cumple.
     *
     * @param usuario el usuario candidato a deshabilitar.
     * @param adminId UUID del administrador que ejecuta la operación.
     * @param repo    repositorio de usuarios (para consultas de conteo, etc.).
     */
    void validar(Usuario usuario, UUID adminId, UsuarioRepository repo);
}
