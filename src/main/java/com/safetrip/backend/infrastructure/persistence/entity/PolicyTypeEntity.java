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
@Table(name = "policy_types", indexes = {
        @Index(name = "idx_policies_type", columnList = "policy_type_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_type_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyTypeId;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    @ToString.Include
    private String name;

    @Column(name = "base_value", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    @ToString.Include
    private BigDecimal baseValue = BigDecimal.ZERO;

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
    @OneToMany(mappedBy = "policyType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyEntity> policies = new ArrayList<>();
}