package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando se viola una regla de validación del dominio
 */
public class InvalidDomainDataException extends DomainException {

    private final String field;

    public InvalidDomainDataException(String field, String message) {
        super(String.format("Dato inválido en campo '%s': %s", field, message));
        this.field = field;
    }

    public String getField() {
        return field;
    }
}