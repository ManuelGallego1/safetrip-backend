package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
}
