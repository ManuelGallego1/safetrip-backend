package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Otp;

import java.util.List;
import java.util.Optional;

public interface OtpRepository {

    Otp save(Otp otp);

    Optional<Otp> findById(Long otpId);

    Optional<Otp> findByUserIdAndCode(Long userId, String code);

    List<Otp> findByUserId(Long userId);

    void delete(Long otpId);
}