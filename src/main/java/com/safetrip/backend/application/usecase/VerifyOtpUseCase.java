package com.safetrip.backend.application.usecase;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.security.JwtService;

public class VerifyOtpUseCase {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public VerifyOtpUseCase(UserRepository userRepository,
                            OtpService otpService,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    public String execute(String phone, String otp) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean valid = otpService.verifyOtp(user, otp);
        if (!valid) {
            throw new RuntimeException("OTP inválido o expirado");
        }

        User activatedUser = user.activate();
        userRepository.save(activatedUser);

        return jwtService.generateToken(user.getPhone());
    }
}