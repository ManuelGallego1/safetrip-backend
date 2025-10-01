package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Otp;
import com.safetrip.backend.domain.repository.OtpRepository;
import com.safetrip.backend.infrastructure.persistence.entity.OtpEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.OtpMapper;
import com.safetrip.backend.infrastructure.persistence.repository.OtpJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OtpRepositoryImpl implements OtpRepository {

    private final OtpJpaRepository otpJpaRepository;

    public OtpRepositoryImpl(OtpJpaRepository otpJpaRepository) {
        this.otpJpaRepository = otpJpaRepository;
    }

    @Override
    public Otp save(Otp otp) {
        OtpEntity entity = OtpMapper.toEntity(otp);
        return OtpMapper.toDomain(otpJpaRepository.save(entity));
    }

    @Override
    public Optional<Otp> findById(Long otpId) {
        return otpJpaRepository.findById(otpId)
                .map(OtpMapper::toDomain);
    }

    @Override
    public Optional<Otp> findByUserIdAndCode(Long userId, String code) {
        return otpJpaRepository.findByUser_UserIdAndCode(userId, code)
                .map(OtpMapper::toDomain);
    }

    @Override
    public List<Otp> findByUserId(Long userId) {
        return otpJpaRepository.findByUser_UserId(userId)
                .stream()
                .map(OtpMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long otpId) {
        otpJpaRepository.deleteById(otpId);
    }
}