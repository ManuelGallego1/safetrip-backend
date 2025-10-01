package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_payments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"payment_fk", "policy_fk"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_payment_id")
    private Long policyPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_fk", nullable = false)
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_fk", nullable = false)
    private PolicyEntity policy;

    @Column(name = "applied_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal appliedAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
