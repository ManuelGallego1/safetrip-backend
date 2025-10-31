package com.safetrip.backend.domain.exception;

public class UserNotActiveException extends DomainException {

    private final Long userId;

    public UserNotActiveException(String message) {
        super(message);
        this.userId = null;
    }

    public UserNotActiveException(Long userId, String message) {
        super(message);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}