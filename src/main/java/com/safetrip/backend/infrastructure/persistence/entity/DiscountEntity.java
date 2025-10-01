package com.safetrip.backend.infrastructure.persistence.entity;

import com.safetrip.backend.domain.model.Discount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "discounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // 'percentage' or 'fixed'

    @Column(name = "value", nullable = false, precision = 18, scale = 4)
    private BigDecimal value;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PolicyEntity> policies;

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