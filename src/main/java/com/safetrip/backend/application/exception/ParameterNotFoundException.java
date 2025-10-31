package com.safetrip.backend.application.exception;

public class ParameterNotFoundException extends RuntimeException {
    public ParameterNotFoundException(String message) {
        super(message);
    }
}