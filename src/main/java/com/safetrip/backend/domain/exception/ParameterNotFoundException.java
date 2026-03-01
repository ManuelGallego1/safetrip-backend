package com.safetrip.backend.domain.exception;

public class ParameterNotFoundException extends RuntimeException {
    public ParameterNotFoundException(String message) {
        super(message);
    }
}