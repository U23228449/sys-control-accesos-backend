package com.estaciona.api.common.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Lanzada cuando las credenciales de login son inválidas.
 * Extiende BadCredentialsException (AuthenticationException) → HTTP 401.
 * El mensaje es genérico para no revelar si el usuario existe o no.
 */
public class InvalidCredentialsException extends BadCredentialsException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas. Verifica tu identificador y contraseña.");
    }
}
