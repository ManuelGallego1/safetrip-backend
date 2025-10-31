package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando un usuario no tiene permisos para realizar una operación
 */
public class UnauthorizedOperationException extends DomainException {

    private final Long userId;
    private final String operation;

    public UnauthorizedOperationException(Long userId, String operation) {
        super(String.format("El usuario %s no tiene permisos para realizar la operación: %s", userId, operation));
        this.userId = userId;
        this.operation = operation;
    }

    public Long getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }
}