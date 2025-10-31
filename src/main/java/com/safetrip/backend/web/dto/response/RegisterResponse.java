package com.safetrip.backend.web.dto.response;

import com.safetrip.backend.domain.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private String fullName;
    private String documentNumber;
    private DocumentType documentType;
    private String email;
    private String phone;
    private String address;
    private String role;
}