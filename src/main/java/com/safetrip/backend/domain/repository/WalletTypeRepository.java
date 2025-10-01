package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.WalletType;

import java.util.List;
import java.util.Optional;

public interface WalletTypeRepository {

    WalletType save(WalletType walletType);

    Optional<WalletType> findById(Long id);

    List<WalletType> findAll();

    void deleteById(Long id);

    Optional<WalletType> findByName(String name);
}