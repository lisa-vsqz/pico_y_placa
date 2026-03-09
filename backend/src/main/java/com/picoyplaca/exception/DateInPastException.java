package com.picoyplaca.exception;

/**
 * Excepción lanzada cuando se proporciona una fecha anterior a la actual.
 * <p>
 * Esta es una excepción de negocio, no técnica, por lo que se maneja
 * retornando un HTTP 422 (Unprocessable Entity).
 */
public class DateInPastException extends RuntimeException {

    public DateInPastException(String message) {
        super(message);
    }
}
