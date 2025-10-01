package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "wallet_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_type_id")
    private Long walletTypeId;

    @Column(name = "name", nullable = false, unique = true, length = 150)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "walletType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WalletEntity> wallets;
}