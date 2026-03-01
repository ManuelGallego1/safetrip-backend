package com.safetrip.backend.domain.exception;

/**
 * Excepción de dominio para errores relacionados con el envío de emails
 */
public class EmailException extends RuntimeException {

    public EmailException(String message) {
        super(message);
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}