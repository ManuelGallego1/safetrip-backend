package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.WalletTransaction;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository {
    WalletTransaction save(WalletTransaction transaction);
    Optional<WalletTransaction> findById(Long id);
    Optional<WalletTransaction> findByPayment(Payment payment);
    List<WalletTransaction> findByWalletId(Long walletId);
    void deleteById(Long id);

    void updateTransactionOnConfirm(
            Long transactionId,
            BigDecimal balance,
            String description,
            ZonedDateTime updatedAt
    );

    int countByWalletId (Long walletId);
}