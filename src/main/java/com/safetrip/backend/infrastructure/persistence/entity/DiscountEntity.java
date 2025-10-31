package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.model.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long discountId;

    @Column(name = "name", nullable = false, length = 150)
    @ToString.Include
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    @ToString.Include
    private DiscountType type; // 'percentage' or 'fixed'

    @Column(name = "value", nullable = false, precision = 18, scale = 4)
    @ToString.Include
    private BigDecimal value;

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

    // Relationships
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyEntity> policies = new ArrayList<>();

    // Domain conversion methods
    public Discount toDomain() {
        return new Discount(
                this.discountId,
                this.name,
                this.type,
                this.value,
                this.active,
                this.createdAt,
                this.updatedAt
        );
    }

    public static DiscountEntity fromDomain(Discount discount) {
        return DiscountEntity.builder()
                .discountId(discount.getDiscountId())
                .name(discount.getName())
                .type(discount.getType())
                .value(discount.getValue())
                .active(discount.getActive())
                .createdAt(discount.getCreatedAt())
                .updatedAt(discount.getUpdatedAt())
                .build();
    }
}