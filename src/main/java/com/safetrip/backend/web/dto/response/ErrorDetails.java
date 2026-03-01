package com.safetrip.backend.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private int status;
    private String error;
    private String message;
    private String field;           // Para errores de campo específico (ej: email duplicado)
    private String value;           // Valor que causó el error
    private Map<String, String> details; // Para validaciones múltiples
    private LocalDateTime timestamp;

    public static ErrorDetails of(int status, String error, String message) {
        return ErrorDetails.builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
