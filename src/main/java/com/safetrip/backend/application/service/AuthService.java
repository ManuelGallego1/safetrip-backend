package com.safetrip.backend.domain.service;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.LoginOtpRequest;
import com.safetrip.backend.web.dto.request.LoginRequest;
import com.safetrip.backend.web.dto.request.RegisterRequest;

public interface AuthService {
    String login(LoginRequest loginRequest);
    String loginOtp(LoginOtpRequest loginRequest);
    User registerUser(RegisterRequest registerRequest);
}