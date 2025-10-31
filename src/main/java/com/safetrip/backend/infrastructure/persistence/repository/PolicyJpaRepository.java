package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PolicyJpaRepository extends JpaRepository<PolicyEntity, Long> {

    Optional<PolicyEntity> findByPolicyNumber(String policyNumber);

    @Query("""
        SELECT p FROM PolicyEntity p
        WHERE p.createdByUser.userId = :userId
        ORDER BY p.createdAt DESC
    """)
    Page<PolicyEntity> findByCreatedByUserUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE PolicyEntity p SET " +
            "p.policyNumber = COALESCE(:policyNumber, p.policyNumber), " +
            "p.updatedAt = COALESCE(:updatedAt, p.updatedAt), " +
            "p.unitPriceWithDiscount = COALESCE(:unitPrice, p.unitPriceWithDiscount), " +
            "p.personCount = COALESCE(:personCount, p.personCount) " +
            "WHERE p.policyId = :policyId")
    int patchPolicy(
            @Param("policyId") Long policyId,
            @Param("policyNumber") String policyNumber,
            @Param("updatedAt") ZonedDateTime updatedAt,
            @Param("unitPrice") BigDecimal unitPrice,
            @Param("personCount") Integer personCount
    );
}
