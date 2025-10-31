package com.safetrip.backend.web.exception;

import com.safetrip.backend.web.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.safetrip.backend.application.exception.*;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Integer>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Integer>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), 401));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse<Integer>> handleUserNotActive(UserNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), 403));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Integer>> handleIOException(IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error de entrada/salida: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Integer>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", 500));
    }

    @ExceptionHandler(ParameterNotFoundException.class)
    public ResponseEntity<ApiResponse<Integer>> handleParameterNotFound(ParameterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(ProcessNotFoundException.class)
    public ResponseEntity<ApiResponse<Integer>> handleProcessNotFound(ProcessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

}