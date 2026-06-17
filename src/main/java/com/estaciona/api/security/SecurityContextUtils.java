package com.estaciona.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utilidad estática para obtener el usuario autenticado actual desde el SecurityContext.
 * Nunca recibe el usuarioId como parámetro de request.
 */
public class SecurityContextUtils {

    private SecurityContextUtils() {
        // Clase utilitaria, no instanciar
    }

    /**
     * Retorna el UUID del usuario autenticado actual desde el JWT en el SecurityContext.
     *
     * @throws IllegalStateException si no hay usuario autenticado.
     */
    public static UUID obtenerUsuarioIdActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) autenticacion.getPrincipal();
        return userDetails.obtenerUsuarioId();
    }
}
