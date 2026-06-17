package com.estaciona.api.common.exception;

/**
 * Lanzada cuando no se encuentra un recurso solicitado. → HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
