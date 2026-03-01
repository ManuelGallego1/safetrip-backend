package com.safetrip.backend.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpErrorResponse {
    private String error;
    private boolean locked;
    private Integer remainingAttempts;
    private Long lockoutSeconds;
    private String message;

    public static OtpErrorResponse accountLocked(long lockoutSeconds) {
        return new OtpErrorResponse(
                "ACCOUNT_LOCKED",
                true,
                0,
                lockoutSeconds,
                "Cuenta bloqueada temporalmente. Intenta en " + lockoutSeconds + " segundos."
        );
    }

    public static OtpErrorResponse invalidOtp(int remainingAttempts) {
        return new OtpErrorResponse(
                "INVALID_OTP",
                false,
                remainingAttempts,
                null,
                "Código incorrecto. Te quedan " + remainingAttempts + " intentos."
        );
    }

    public static OtpErrorResponse maxAttemptsReached(long lockoutSeconds) {
        return new OtpErrorResponse(
                "MAX_ATTEMPTS_REACHED",
                true,
                0,
                lockoutSeconds,
                "Demasiados intentos fallidos. Cuenta bloqueada por " + (lockoutSeconds / 60) + " minuto(s)."
        );
    }
}