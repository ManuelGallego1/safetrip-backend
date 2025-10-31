package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_payments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"payment_fk", "policy_fk"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_payment_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_fk", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_fk", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private PolicyEntity policy;

    @Column(name = "applied_amount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    @ToString.Include
    private BigDecimal appliedAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}