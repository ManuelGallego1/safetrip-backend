package com.safetrip.backend.application.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.response.LoginResponse;
import com.safetrip.backend.web.dto.response.RegisterResponse;

public class LoginResponseMapper {

    private LoginResponseMapper() {
    }

    public static LoginResponse toDto(User user, String token) {
        RegisterResponse registerResponse = new RegisterResponse(
                user.getUserId(),
                user.getPerson().getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getName()
        );

        return new LoginResponse(registerResponse, token);
    }
}