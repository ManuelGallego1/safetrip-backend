package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.WalletTransaction;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction walletTransaction);

    Optional<WalletTransaction> findById(Long id);

    List<WalletTransaction> findAll();

    void deleteById(Long id);

    List<WalletTransaction> findByWalletId(Long walletId);

    List<WalletTransaction> findByPaymentId(Long paymentId);
}