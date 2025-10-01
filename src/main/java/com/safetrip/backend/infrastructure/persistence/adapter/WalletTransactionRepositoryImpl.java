package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.WalletTransaction;
import com.safetrip.backend.domain.repository.WalletTransactionRepository;
import com.safetrip.backend.infrastructure.persistence.entity.WalletTransactionEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.WalletTransactionMapper;
import com.safetrip.backend.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WalletTransactionRepositoryImpl implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository walletTransactionJpaRepository;

    public WalletTransactionRepositoryImpl(WalletTransactionJpaRepository walletTransactionJpaRepository) {
        this.walletTransactionJpaRepository = walletTransactionJpaRepository;
    }

    @Override
    public WalletTransaction save(WalletTransaction walletTransaction) {
        WalletTransactionEntity entity = WalletTransactionMapper.toEntity(walletTransaction);
        return WalletTransactionMapper.toDomain(walletTransactionJpaRepository.save(entity));
    }

    @Override
    public Optional<WalletTransaction> findById(Long id) {
        return walletTransactionJpaRepository.findById(id)
                .map(WalletTransactionMapper::toDomain);
    }

    @Override
    public List<WalletTransaction> findAll() {
        return walletTransactionJpaRepository.findAll()
                .stream()
                .map(WalletTransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        walletTransactionJpaRepository.deleteById(id);
    }

    @Override
    public List<WalletTransaction> findByWalletId(Long walletId) {
        return walletTransactionJpaRepository.findByWallet_WalletId(walletId)
                .stream()
                .map(WalletTransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletTransaction> findByPaymentId(Long paymentId) {
        return walletTransactionJpaRepository.findByPayment_PaymentId(paymentId)
                .stream()
                .map(WalletTransactionMapper::toDomain)
                .collect(Collectors.toList());
    }
}