package com.safetrip.backend.application.exception;

public class ProcessNotFoundException extends RuntimeException {
    public ProcessNotFoundException(String message) {
        super(message);
    }
}