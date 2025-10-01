package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.impl.AuthServiceImpl;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.request.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", token));
    }
}