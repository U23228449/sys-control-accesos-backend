package com.estaciona.api.common.exception;

/**
 * Lanzada cuando se intenta crear un recurso que ya existe (placa, correo, documento). → HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super(recurso + " ya existe con " + campo + ": " + valor);
    }
}
