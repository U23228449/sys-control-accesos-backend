package com.estaciona.api.common.exception;

/**
 * Lanzada cuando el usuario autenticado no tiene permiso para la operación. → HTTP 403.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String mensaje) {
        super(mensaje);
    }
}
