package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.model.User;

public interface OtpService {
    Otp generateOtp(User user, int expirationMinutes);
    boolean verifyOtp(User user, String code);
    void cleanupExpiredOtps(User user);
}