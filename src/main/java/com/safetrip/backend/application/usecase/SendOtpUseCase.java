package com.safetrip.backend.application.usecase;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.enums.NotificationType;
import com.safetrip.backend.domain.service.WhatsAppService;
import com.safetrip.backend.infrastructure.integration.notification.resend.client.EmailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendOtpUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SendOtpUseCase.class);

    private final OtpService otpService;
    private final WhatsAppService whatsAppService;
    private final EmailClient emailClient;

    public SendOtpUseCase(
            OtpService otpService,
            WhatsAppService whatsAppService,
            EmailClient emailClient
    ) {
        this.otpService = otpService;
        this.whatsAppService = whatsAppService;
        this.emailClient = emailClient;
    }

    public void execute(User user, NotificationType notificationType) {
        Otp otp = otpService.generateOtp(user, 5);
        boolean emailSuccess;
        boolean whatsAppSuccess;

        switch (notificationType) {
            case EMAIL:
                emailSuccess = sendEmailOtp(user, otp);

                if (!emailSuccess) {
                    logger.error("Falló el envío de OTP por el canal de email: {}", user.getEmail());
                    throw new OtpDeliveryException(
                            "No se pudo enviar el código OTP. Por favor, intenta nuevamente."
                    );
                }
                break;
            case WHATSAPP:
                whatsAppSuccess = sendWhatsAppOtp(user, otp);

                if (!whatsAppSuccess) {
                    logger.error("Falló el envío de OTP por el canal de whatsapp: {}", user.getPhone());
                    throw new OtpDeliveryException(
                            "No se pudo enviar el código OTP. Por favor, intenta nuevamente."
                    );
                }
                break;
            case ALL:
                emailSuccess = sendEmailOtp(user, otp);
                whatsAppSuccess = sendWhatsAppOtp(user, otp);

                if (!whatsAppSuccess && !emailSuccess) {
                    logger.error("Falló el envío de OTP por ambos canales para el usuario: {}", user.getEmail());
                    throw new OtpDeliveryException(
                            "No se pudo enviar el código OTP. Por favor, intenta nuevamente."
                    );
                }
                break;
            case WHATSAPP_WITH_EMAIL_FALLBACK:
                whatsAppSuccess = sendWhatsAppOtp(user, otp);

                if (!whatsAppSuccess) {
                    logger.warn("Falló el envío de OTP por WhatsApp para el usuario: {}. Intentando por email...", user.getEmail());
                    emailSuccess = sendEmailOtp(user, otp);

                    if (!emailSuccess) {
                        logger.error("Falló el envío de OTP por ambos canales para el usuario: {}", user.getEmail());
                        throw new OtpDeliveryException(
                                "No se pudo enviar el código OTP. Por favor, intenta nuevamente."
                        );
                    }
                    logger.info("OTP enviado exitosamente por Email como fallback para el usuario: {}", user.getEmail());
                } else {
                    logger.info("OTP enviado exitosamente por WhatsApp para el usuario: {}", user.getEmail());
                }
                break;
        }
    }

    /**
     * Intenta enviar el OTP por WhatsApp
     * @return true si fue exitoso, false si falló
     */
    private boolean sendWhatsAppOtp(User user, Otp otp) {
        try {
            whatsAppService.sendOTP(user.getPhone(), otp.getCode());
            logger.info("OTP enviado por WhatsApp al usuario: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar OTP por WhatsApp al usuario: {}", user.getEmail(), e);
            return false;
        }
    }

    /**
     * Intenta enviar el OTP por Email
     * @return true si fue exitoso, false si falló
     */
    private boolean sendEmailOtp(User user, Otp otp) {
        try {
            emailClient.sendOtpEmail(
                    user.getEmail(),
                    user.getPerson().getFullName(),
                    otp.getCode(),
                    5
            );
            logger.info("OTP enviado por Email al usuario: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar OTP por Email al usuario: {}", user.getEmail(), e);
            return false;
        }
    }

    /**
     * Excepción personalizada para errores en la entrega de OTP
     */
    public static class OtpDeliveryException extends RuntimeException {
        public OtpDeliveryException(String message) {
            super(message);
        }
    }
}