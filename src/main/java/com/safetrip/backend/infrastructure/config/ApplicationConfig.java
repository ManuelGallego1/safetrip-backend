package com.safetrip.backend.infrastructure.config;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.application.usecase.SendOtpUseCase;
import com.safetrip.backend.application.usecase.VerifyOtpUseCase;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.domain.service.NotificationService;
import com.safetrip.backend.domain.service.WhatsAppService;
import com.safetrip.backend.infrastructure.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public SendOtpUseCase sendOtpUseCase(
            OtpService otpService,
            WhatsAppService whatsAppService,
            NotificationService notificationService
    ) {
        return new SendOtpUseCase(otpService, whatsAppService, notificationService);
    }

    @Bean
    public VerifyOtpUseCase verifyOtpUseCase(UserRepository userRepository,
                                             OtpService otpService,
                                             JwtService jwtService) {
        return new VerifyOtpUseCase(userRepository, otpService, jwtService);
    }
}