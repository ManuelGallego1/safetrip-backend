package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.exception.InvalidCredentialsException;
import com.safetrip.backend.web.dto.mapper.RegisterResponseMapper;
import com.safetrip.backend.application.service.impl.AuthServiceImpl;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.LoginOtpRequest;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import com.safetrip.backend.web.dto.request.SendOtpRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.request.LoginRequest;
import com.safetrip.backend.web.dto.response.LoginResponse;
import com.safetrip.backend.web.dto.response.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse responseDTO = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", responseDTO));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@RequestBody LoginOtpRequest request) {
        LoginResponse token = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", token));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        User user = authService.registerUser(request);

        RegisterResponse responseDTO = RegisterResponseMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", responseDTO));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody SendOtpRequest request) {
        String message = authService.sendOtp(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", message));
    }


}