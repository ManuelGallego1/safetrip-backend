package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_type_fk")
    @ToString.Exclude
    private PolicyTypeEntity policyType;

    @Column(name = "person_count", nullable = false)
    @Builder.Default
    @ToString.Include
    private Integer personCount = 1;

    @Column(name = "unit_price_with_discount", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal unitPriceWithDiscount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_fk")
    @ToString.Exclude
    private DiscountEntity discount;

    @Column(name = "policy_number", unique = true, length = 120)
    @ToString.Include
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_fk")
    @ToString.Exclude
    private UserEntity createdByUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "created_with_file", nullable = false)
    private Boolean createdWithFile;

    // Relaciones
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyDetailEntity> policyDetails = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyPaymentEntity> policyPayments = new ArrayList<>();

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyPersonEntity> policyPersons = new ArrayList<>();
}