package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "otps", uniqueConstraints = {
        @UniqueConstraint(name = "INDEX_otp_user_code", columnNames = {"user_fk", "code"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_fk", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "code", nullable = false, length = 50)
    @ToString.Exclude  // 🔒 SEGURIDAD: No mostrar códigos OTP en logs
    private String code;

    @Column(name = "expiration", nullable = false)
    private ZonedDateTime expiration;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    @ToString.Include
    private Boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}