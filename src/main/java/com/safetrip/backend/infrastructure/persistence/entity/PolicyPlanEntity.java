package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_plan_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyPlanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_type_fk", nullable = false)
    @ToString.Exclude
    private PolicyTypeEntity policyType;

    @Column(name = "pax", nullable = false)
    @ToString.Include
    private Integer pax;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    @ToString.Include
    private BigDecimal discountPercentage;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "popular", nullable = false)
    @Builder.Default
    @ToString.Include
    private Boolean popular = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    @ToString.Include
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}