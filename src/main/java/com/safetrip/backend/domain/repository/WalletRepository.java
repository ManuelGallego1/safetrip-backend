package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long walletId);

    List<Wallet> findAll();

    void deleteById(Long walletId);

    Optional<Wallet> findByUserId(Long userId);
}