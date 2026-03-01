package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletJpaRepository extends JpaRepository<WalletEntity, Long> {

    Optional<WalletEntity> findByUser_UserId(Long userId);

    List<WalletEntity> findAllByUser_UserId(Long userId);

    List<WalletEntity> findByUser_UserIdAndWalletType_WalletTypeId(Long userId, Long walletTypeId);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.transactionId = :transactionId, w.updatedAt = :updatedAt WHERE w.walletId = :walletId")
    int updateTransactionId(@Param("walletId") Long walletId,
                            @Param("transactionId") String transactionId,
                            @Param("updatedAt") ZonedDateTime updatedAt);

    @Modifying
    @Query("UPDATE WalletEntity w SET w.total = :newBalance, w.updatedAt = :updatedAt WHERE w.walletId = :walletId")
    int updateBalance(@Param("walletId") Long walletId,
                      @Param("newBalance") BigDecimal newBalance,
                      @Param("updatedAt") ZonedDateTime updatedAt);
}