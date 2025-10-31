package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando se intenta activar/desactivar un usuario incorrectamente
 */
public class InvalidUserStateException extends DomainException {

    private final Long userId;
    private final String currentState;
    private final String attemptedState;

    public InvalidUserStateException(Long userId, String currentState, String attemptedState) {
        super(String.format("No se puede cambiar el usuario %s del estado '%s' a '%s'",
                userId, currentState, attemptedState));
        this.userId = userId;
        this.currentState = currentState;
        this.attemptedState = attemptedState;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedState() {
        return attemptedState;
    }
}