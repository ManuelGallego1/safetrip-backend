package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.WalletTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletTypeJpaRepository extends JpaRepository<WalletTypeEntity, Long> {

    Optional<WalletTypeEntity> findByName(String name);
}