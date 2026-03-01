package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.enums.NotificationType;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.persistence.adapter.UserRepositoryImpl;
import com.safetrip.backend.web.dto.mapper.RegisterResponseMapper;
import com.safetrip.backend.application.service.impl.AuthServiceImpl;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.*;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.LoginResponse;
import com.safetrip.backend.web.dto.response.OtpErrorResponse;
import com.safetrip.backend.web.dto.response.RegisterResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthServiceImpl authService;
    private final OtpService otpService;
    private final UserRepositoryImpl userRepository;

    public AuthController(AuthServiceImpl authService, OtpService otpService, UserRepositoryImpl userRepository) {
        this.authService = authService;
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse responseDTO = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", responseDTO));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody LoginOtpRequest request) {
        if ((request.getPhone() == null || request.getPhone().isEmpty()) &&
                (request.getEmail() == null || request.getEmail().isEmpty())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Debe proporcionar un email o teléfono", null));
        }

        try {
            LoginResponse token = authService.verifyOtp(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", token));
        } catch (RuntimeException e) {
            // ✅ Manejar errores de bloqueo y intentos fallidos
            String errorMsg = e.getMessage();

            if (errorMsg.contains("Cuenta bloqueada")) {
                User user = getUserFromRequest(request);
                if (user != null) {
                    long lockoutSeconds = otpService.getRemainingLockoutSeconds(user.getUserId());
                    OtpErrorResponse error = OtpErrorResponse.accountLocked(lockoutSeconds);
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiResponse.error(errorMsg, error));
                }
            } else if (errorMsg.contains("Demasiados intentos")) {
                User user = getUserFromRequest(request);
                if (user != null) {
                    long lockoutSeconds = otpService.getRemainingLockoutSeconds(user.getUserId());
                    OtpErrorResponse error = OtpErrorResponse.maxAttemptsReached(lockoutSeconds);
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiResponse.error(errorMsg, error));
                }
            }

            // Error genérico de OTP inválido
            User user = getUserFromRequest(request);
            if (user != null) {
                int remaining = otpService.getRemainingAttempts(user.getUserId());
                OtpErrorResponse error = OtpErrorResponse.invalidOtp(remaining);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Código OTP inválido", error));
            }

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMsg, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        User user = authService.registerUser(request);
        RegisterResponse responseDTO = RegisterResponseMapper.toDto(user);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", responseDTO));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody SendOtpRequest request) {
        try {
            String message = authService.sendOtp(request.getPhone());
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", message));
        } catch (RuntimeException e) {
            // ✅ Verificar si está bloqueado al intentar reenviar OTP
            if (e.getMessage().contains("Cuenta bloqueada")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error(e.getMessage(), null));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody SendOtpRequest request) {
        NotificationType notificationType;
        String message;

        try {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                notificationType = NotificationType.EMAIL;
                message = authService.sendPasswordResetOtp(request.getEmail(), notificationType);
            } else if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                notificationType = NotificationType.WHATSAPP;
                message = authService.sendPasswordResetOtp(request.getPhone(), notificationType);
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Debe proporcionar un email o teléfono", null));
            }

            return ResponseEntity.ok(ApiResponse.success("OTP de recuperación enviado", message));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cuenta bloqueada")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error(e.getMessage(), null));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<?>> verifyResetOtp(@RequestBody LoginOtpRequest request) {
        try {
            LoginResponse response = authService.verifyOtp(request);
            return ResponseEntity.ok(ApiResponse.success("OTP verificado correctamente", response));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cuenta bloqueada")) {
                User user = getUserFromRequest(request);
                if (user != null) {
                    long lockoutSeconds = otpService.getRemainingLockoutSeconds(user.getUserId());
                    OtpErrorResponse error = OtpErrorResponse.accountLocked(lockoutSeconds);
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(ApiResponse.error(e.getMessage(), error));
                }
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(user, request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada exitosamente", message));
    }

    // ✅ Método auxiliar para obtener usuario del request
    private User getUserFromRequest(LoginOtpRequest request) {
        return userRepository.findByEmail(request.getEmail()).orElseThrow();
    }
}