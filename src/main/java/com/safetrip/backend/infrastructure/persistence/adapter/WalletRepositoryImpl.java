package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.domain.repository.WalletRepository;
import com.safetrip.backend.infrastructure.persistence.entity.WalletEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.WalletMapper;
import com.safetrip.backend.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    @Override
    @Transactional
    public Wallet save(Wallet wallet) {
        WalletEntity entity = WalletMapper.toEntity(wallet);
        WalletEntity savedEntity = walletJpaRepository.save(entity);
        return WalletMapper.toDomain(savedEntity);
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
    @Transactional
    public void deleteById(Long walletId) {
        walletJpaRepository.deleteById(walletId);
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return walletJpaRepository.findByUser_UserId(userId)
                .map(WalletMapper::toDomain);
    }

    @Override
    public List<Wallet> findByUserAndWalletType(Long userId, Long walletTypeId) {
        return walletJpaRepository.findByUser_UserIdAndWalletType_WalletTypeId(userId, walletTypeId).stream().map(WalletMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Wallet> findAllByUserId(Long userId) {
        return walletJpaRepository.findAllByUser_UserId(userId)
                .stream()
                .map(WalletMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateTotal(Long walletId, BigDecimal total) {
        WalletEntity entity = walletJpaRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

        entity.setTotal(total);
        entity.setUpdatedAt(ZonedDateTime.now());

        walletJpaRepository.save(entity);
    }

    @Override
    @Transactional
    public int updateTransactionId(Long walletId, String transactionId, ZonedDateTime updatedAt) {
        return walletJpaRepository.updateTransactionId(walletId, transactionId, updatedAt);
    }

    @Override
    @Transactional
    public int updateBalance(Long walletId, BigDecimal newBalance, ZonedDateTime updatedAt) {
        return walletJpaRepository.updateBalance(walletId, newBalance, updatedAt);
    }
}