package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un usuario en el sistema
 */
public class UserNotFoundException extends DomainException {

    private final String identifier;

    public UserNotFoundException(String identifier) {
        super(String.format("Usuario no encontrado: %s", identifier));
        this.identifier = identifier;
    }

    public UserNotFoundException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}