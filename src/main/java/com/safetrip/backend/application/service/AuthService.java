package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.LoginOtpRequest;
import com.safetrip.backend.web.dto.request.LoginRequest;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import com.safetrip.backend.web.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    LoginResponse verifyOtp(LoginOtpRequest loginRequest);
    User registerUser(RegisterRequest registerRequest);
    String sendOtp(String phoneNumber);
    LoginResponse getUserInfo(User user);
}