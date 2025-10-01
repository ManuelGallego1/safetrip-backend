package com.safetrip.backend.web.dto.request;

import com.safetrip.backend.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    private PersonRequest person;

    // Datos del usuario
    private String email;
    private String phone;
    private String password;
    private String role;
}