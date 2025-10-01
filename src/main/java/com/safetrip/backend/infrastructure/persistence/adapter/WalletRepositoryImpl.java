package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.domain.repository.WalletRepository;
import com.safetrip.backend.infrastructure.persistence.entity.WalletEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.WalletMapper;
import com.safetrip.backend.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    public WalletRepositoryImpl(WalletJpaRepository walletJpaRepository) {
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = WalletMapper.toEntity(wallet);
        return WalletMapper.toDomain(walletJpaRepository.save(entity));
    }

    @Override
    public Optional<Wallet> findById(Long walletId) {
        return walletJpaRepository.findById(walletId)
                .map(WalletMapper::toDomain);
    }

    @Override
    public List<Wallet> findAll() {
        return walletJpaRepository.findAll()
                .stream()
                .map(WalletMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long walletId) {
        walletJpaRepository.deleteById(walletId);
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return walletJpaRepository.findByUser_UserId(userId)
                .map(WalletMapper::toDomain);
    }
}