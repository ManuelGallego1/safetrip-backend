package com.safetrip.backend.application.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.response.RegisterResponse;

public class RegisterResponseMapper {

    public static RegisterResponse toDto(User user) {
        return new RegisterResponse(
                user.getUserId(),
                user.getPerson().getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getName()
        );
    }
}