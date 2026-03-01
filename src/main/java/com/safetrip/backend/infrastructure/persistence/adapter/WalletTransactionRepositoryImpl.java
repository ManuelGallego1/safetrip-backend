package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.WalletTransaction;
import com.safetrip.backend.domain.repository.WalletTransactionRepository;
import com.safetrip.backend.infrastructure.persistence.entity.WalletTransactionEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.WalletTransactionMapper;
import com.safetrip.backend.infrastructure.persistence.repository.WalletTransactionJpaRepository;
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
public class WalletTransactionRepositoryImpl implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository walletTransactionJpaRepository;

    @Override
    public WalletTransaction save(WalletTransaction transaction) {
        WalletTransactionEntity entity = WalletTransactionMapper.toEntity(transaction);
        return WalletTransactionMapper.toDomain(walletTransactionJpaRepository.save(entity));
    }

    @Override
    public Optional<WalletTransaction> findById(Long id) {
        return walletTransactionJpaRepository.findById(id)
                .map(WalletTransactionMapper::toDomain);
    }

    @Override
    public Optional<WalletTransaction> findByPayment(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            return Optional.empty();
        }

        return walletTransactionJpaRepository
                .findByPayment_PaymentId(payment.getPaymentId())
                .stream()
                .findFirst()
                .map(WalletTransactionMapper::toDomain);
    }

    @Override
    public List<WalletTransaction> findByWalletId(Long walletId) {
        return walletTransactionJpaRepository.findByWallet_WalletId(walletId)
                .stream()
                .map(WalletTransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        walletTransactionJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateTransactionOnConfirm(
            Long transactionId,
            BigDecimal balance,
            String description,
            ZonedDateTime updatedAt
    ) {
        walletTransactionJpaRepository.findById(transactionId).ifPresent(entity -> {
            entity.setBalance(balance);
            entity.setDescription(description);
            entity.setUpdatedAt(updatedAt);
            walletTransactionJpaRepository.save(entity);
        });
    }

    @Override
    public int countByWalletId(Long walletId) {
        return walletTransactionJpaRepository.countByWalletId(walletId);
    }
}