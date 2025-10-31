package com.safetrip.backend.web.dto.mapper;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.response.RegisterResponse;

public class RegisterResponseMapper {

    public static RegisterResponse toDto(User user) {
        return new RegisterResponse(
                user.getPerson().getFullName(),
                user.getPerson().getDocumentNumber(),
                user.getPerson().getDocumentType(),
                user.getEmail(),
                user.getPhone(),
                user.getPerson().getAddress(),
                user.getRole().getName()
        );
    }
}