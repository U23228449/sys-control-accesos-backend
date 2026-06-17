package com.estaciona.api.common.exception;

/**
 * Lanzada cuando se viola una regla de negocio (ej. aforo lleno, estado inválido). → HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}
