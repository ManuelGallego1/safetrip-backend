package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<Long, Integer> failedAttempts = new HashMap<>();
    private final Map<Long, ZonedDateTime> lockoutExpiry = new HashMap<>();

    private static final String ALLOWED_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int OTP_LENGTH = 6;
    private static final int DEFAULT_EXPIRATION_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 1;

    public OtpServiceImpl(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public Otp generateOtp(User user, int expirationMinutes) {
        Long userId = user.getUserId();

        // ✅ Verificar si la cuenta está bloqueada ANTES de generar OTP
        if (isAccountLocked(userId)) {
            throw new RuntimeException("Cuenta bloqueada temporalmente. Intenta en " +
                    getRemainingLockoutSeconds(userId) + " segundos.");
        }

        String code = generateSecureCode();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiration = now.plusMinutes(expirationMinutes);

        Otp otp = new Otp(
                null,
                user,
                code,
                expiration,
                false,
                now,
                now
        );

        return otpRepository.save(otp);
    }

    public Otp generateOtp(User user) {
        return generateOtp(user, DEFAULT_EXPIRATION_MINUTES);
    }

    private String generateSecureCode() {
        StringBuilder code = new StringBuilder(OTP_LENGTH);

        for (int i = 0; i < OTP_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(ALLOWED_CHARS.length());
            code.append(ALLOWED_CHARS.charAt(randomIndex));
        }

        return code.toString();
    }

    public boolean verifyOtp(User user, String code) {
        Long userId = user.getUserId();

        // ✅ Verificar si la cuenta está bloqueada
        if (isAccountLocked(userId)) {
            throw new RuntimeException("Cuenta bloqueada temporalmente. Intenta en " +
                    getRemainingLockoutSeconds(userId) + " segundos.");
        }

        String normalizedCode = code.toUpperCase().trim();
        Optional<Otp> otpOpt = otpRepository.findByUserIdAndCode(userId, normalizedCode);

        if (otpOpt.isEmpty()) {
            recordFailedAttempt(userId);
            return false;
        }

        Otp otp = otpOpt.get();

        if (otp.isVerified() || otp.isExpired()) {
            recordFailedAttempt(userId);
            return false;
        }

        // ✅ Verificación exitosa: resetear intentos
        resetFailedAttempts(userId);

        Otp verifiedOtp = new Otp(
                otp.getOtpId(),
                otp.getUser(),
                otp.getCode(),
                otp.getExpiration(),
                true,
                otp.getCreatedAt(),
                ZonedDateTime.now()
        );

        otpRepository.save(verifiedOtp);
        return true;
    }

    public boolean isAccountLocked(Long userId) {
        ZonedDateTime lockout = lockoutExpiry.get(userId);
        if (lockout == null) {
            return false;
        }

        if (ZonedDateTime.now().isAfter(lockout)) {
            lockoutExpiry.remove(userId);
            resetFailedAttempts(userId);
            return false;
        }

        return true;
    }

    public long getRemainingLockoutSeconds(Long userId) {
        ZonedDateTime lockout = lockoutExpiry.get(userId);
        if (lockout == null) {
            return 0;
        }

        long seconds = ZonedDateTime.now().until(lockout, java.time.temporal.ChronoUnit.SECONDS);
        return Math.max(0, seconds);
    }

    public void recordFailedAttempt(Long userId) {
        int attempts = failedAttempts.getOrDefault(userId, 0) + 1;
        failedAttempts.put(userId, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            ZonedDateTime lockout = ZonedDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            lockoutExpiry.put(userId, lockout);
            throw new RuntimeException("Demasiados intentos fallidos. Cuenta bloqueada por " +
                    LOCKOUT_MINUTES + " minuto(s).");
        }
    }

    public void resetFailedAttempts(Long userId) {
        failedAttempts.remove(userId);
        lockoutExpiry.remove(userId);
    }

    public int getRemainingAttempts(Long userId) {
        int attempts = failedAttempts.getOrDefault(userId, 0);
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public void cleanupExpiredOtps(User user) {
        otpRepository.findByUserId(user.getUserId())
                .stream()
                .filter(Otp::isExpired)
                .forEach(otp -> otpRepository.delete(otp.getOtpId()));
    }
}