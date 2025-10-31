package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear o actualizar un usuario
 * con un email o teléfono que ya existe en el sistema
 */
public class UserAlreadyExistsException extends DomainException {

    private final String field;
    private final String value;

    public UserAlreadyExistsException(String field, String value) {
        super(String.format("Ya existe un usuario con %s: %s", field, value));
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}