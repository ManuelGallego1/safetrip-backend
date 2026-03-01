package com.safetrip.backend.domain.exception;

public class DuplicateDiscountNameException extends RuntimeException {
    public DuplicateDiscountNameException(String message) {
        super(message);
    }
}