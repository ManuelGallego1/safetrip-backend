package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PaymentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTypeJpaRepository extends JpaRepository<PaymentTypeEntity, Long> {

    Optional<PaymentTypeEntity> findByName(String name);
}