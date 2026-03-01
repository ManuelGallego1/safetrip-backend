package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Wallet;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface WalletRepository {
    // Operaciones básicas CRUD
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(Long walletId);
    List<Wallet> findAll();
    void deleteById(Long walletId);

    // Consultas específicas de negocio
    Optional<Wallet> findByUserId(Long userId);
    List<Wallet> findByUserAndWalletType(Long userId, Long walletTypeId);
    List<Wallet> findAllByUserId(Long userId);

    // Operaciones de actualización
    void updateTotal(Long walletId, BigDecimal total);

    // Nuevo método para actualizar el transactionId
    int updateTransactionId(Long walletId, String transactionId, ZonedDateTime updatedAt);

    int updateBalance(Long walletId, BigDecimal newBalance,  ZonedDateTime updatedAt);
}