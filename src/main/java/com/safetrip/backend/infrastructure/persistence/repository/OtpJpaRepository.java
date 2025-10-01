package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpJpaRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByUser_UserIdAndCode(Long userId, String code);

    List<OtpEntity> findByUser_UserId(Long userId);
}