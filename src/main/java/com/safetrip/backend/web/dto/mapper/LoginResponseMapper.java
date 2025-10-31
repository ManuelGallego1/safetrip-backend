package com.safetrip.backend.web.dto.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.response.LoginResponse;
import com.safetrip.backend.web.dto.response.RegisterResponse;

public class LoginResponseMapper {

    private LoginResponseMapper() {
    }

    public static LoginResponse toDto(User user, String token) {
        RegisterResponse registerResponse = new RegisterResponse(
                user.getPerson().getFullName(),
                user.getPerson().getDocumentNumber(),
                user.getPerson().getDocumentType(),
                user.getEmail(),
                user.getPhone(),
                user.getPerson().getAddress(),
                user.getRole().getName()
        );

        return new LoginResponse(registerResponse, token);
    }
}