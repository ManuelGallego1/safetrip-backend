package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Long> {
    Optional<DiscountEntity> findByName(String name);
}