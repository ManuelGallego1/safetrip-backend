package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.OtpRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final Random random = new Random();

    public OtpServiceImpl(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public Otp generateOtp(User user, int expirationMinutes) {
        String code = String.format("%06d", random.nextInt(1_000_000)); // OTP de 6 dígitos
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

    public boolean verifyOtp(User user, String code) {
        Optional<Otp> otpOpt = otpRepository.findByUserIdAndCode(user.getUserId(), code);

        if (otpOpt.isEmpty()) {
            return false;
        }

        Otp otp = otpOpt.get();

        if (otp.isVerified() || otp.isExpired()) {
            return false;
        }

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

    public void cleanupExpiredOtps(User user) {
        otpRepository.findByUserId(user.getUserId())
                .stream()
                .filter(Otp::isExpired)
                .forEach(otp -> otpRepository.delete(otp.getOtpId()));
    }
}