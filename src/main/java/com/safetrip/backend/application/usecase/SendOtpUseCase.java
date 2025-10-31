package com.safetrip.backend.application.usecase;

import com.safetrip.backend.application.dto.NotificationDTO;
import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.service.NotificationService;
import com.safetrip.backend.domain.service.WhatsAppService;

public class SendOtpUseCase {

    private final OtpService otpService;
    private final WhatsAppService whatsAppService;
    private final NotificationService notificationService;

    public SendOtpUseCase(
            OtpService otpService,
            WhatsAppService whatsAppService,
            NotificationService notificationService
    ) {
        this.otpService = otpService;
        this.whatsAppService = whatsAppService;
        this.notificationService = notificationService;
    }

    public void execute(User user) {
        Otp otp = otpService.generateOtp(user, 5);

        whatsAppService.sendOTP(user.getPhone(), otp.getCode());

        /*NotificationDTO notification = new NotificationDTO(
                user.getEmail(),
                "Tu código de verificación es: " + otp.getCode(),
                "Código OTP - Safetrip"
        );
        notificationService.send(notification);*/
    }
}