package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.LoginRequest;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements com.safetrip.backend.domain.service.AuthService {
    @Override
    public String login(LoginRequest loginRequest) {
        return "";
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        return null;
    }
}