package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionEntity, Long> {

    List<WalletTransactionEntity> findByWallet_WalletId(Long walletId);

    List<WalletTransactionEntity> findByPayment_PaymentId(Long paymentId);
}