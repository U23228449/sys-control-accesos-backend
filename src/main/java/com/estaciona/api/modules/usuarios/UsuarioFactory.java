package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.BusinessRuleException;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

/**
 * Factory Method para construir entidades Usuario aplicando reglas por rol.
 */
@Component
public class UsuarioFactory {

    /**
     * Crea una instancia de Usuario a partir del request, rol y hash de contraseña.
     *
     * @param request      DTO con los datos del usuario.
     * @param rol          Rol resuelto y habilitado.
     * @param passwordHash Hash de contraseña generado por PasswordEncoder.
     * @return Entidad Usuario lista para persistir.
     * @throws BusinessRuleException si no se cumplen las reglas de negocio del rol.
     */
    public Usuario crear(UsuarioRequest request, Rol rol, String passwordHash) {
        return crear(request, rol, passwordHash, null, null);
    }

    public Usuario crear(UsuarioRequest request, Rol rol, String passwordHash, com.estaciona.api.modules.campus.entity.Campus campus, com.estaciona.api.modules.zonas.entity.Zona zona) {
        String tipoUsuario = request.tipoUsuario();

        if ("USUARIO".equalsIgnoreCase(rol.getNombre())) {
            // El tipoUsuario es obligatorio si el rol es USUARIO
            if (tipoUsuario == null || tipoUsuario.trim().isEmpty()) {
                throw new BusinessRuleException("El tipo de usuario es obligatorio para el rol USUARIO.");
            }
            tipoUsuario = tipoUsuario.trim().toLowerCase();
        } else {
            // Decisión: Si el rol es administrativo (ADMINISTRADOR, SEGURIDAD, COORDINADOR_SEGURIDAD),
            // ignoramos el tipoUsuario enviado y lo seteamos a null en la entidad,
            // ya que este campo no aplica para dichos roles.
            tipoUsuario = null;
        }

        // Reglas de negocio para guardias
        String tipoGuardia = null;
        if ("SEGURIDAD".equalsIgnoreCase(rol.getNombre()) && zona != null) {
            // Validar que el campus del guardia coincida con el de la zona
            if (campus == null || !campus.getId().equals(zona.getCampus().getId())) {
                throw new BusinessRuleException("El campus asignado debe coincidir con el campus de la zona.");
            }
            if (request.tipoGuardia() == null || request.tipoGuardia().trim().isEmpty()) {
                throw new BusinessRuleException("La función del guardia (entrada o salida) es obligatoria al asignarlo a una zona.");
            }
            tipoGuardia = request.tipoGuardia().trim().toLowerCase();
            if (!"entrada".equals(tipoGuardia) && !"salida".equals(tipoGuardia)) {
                throw new BusinessRuleException("La función del guardia debe ser 'entrada' o 'salida'.");
            }
        }

        return Usuario.builder()
                .rol(rol)
                .nombreCompleto(request.nombreCompleto().trim())
                .correo(request.correo().trim().toLowerCase())
                .documento(request.documento().trim())
                .tipoUsuario(tipoUsuario)
                .passwordHash(passwordHash)
                .enabled(true) // habilitado por defecto
                .campus(campus)
                .zona(zona)
                .tipoGuardia(tipoGuardia)
                .build();
    }
}
