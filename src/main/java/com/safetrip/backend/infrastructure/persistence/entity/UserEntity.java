package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_fk")
    @ToString.Exclude
    private PersonEntity person;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    @ToString.Include
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 512)
    @ToString.Exclude
    @JsonIgnore
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_fk")
    @ToString.Exclude
    private RoleEntity role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    @ToString.Include
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column (name = "profile_image_url")
    @ToString.Include
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<OtpEntity> otps = new ArrayList<>();

    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyEntity> createdPolicies = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<WalletEntity> wallets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PaymentEntity> payments = new ArrayList<>();
}