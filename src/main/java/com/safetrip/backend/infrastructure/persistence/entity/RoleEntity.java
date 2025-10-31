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
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long roleId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @ToString.Include
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<UserEntity> users = new ArrayList<>();

    // Domain conversion methods
    public com.safetrip.backend.domain.model.Role toDomain() {
        return new com.safetrip.backend.domain.model.Role(
                this.roleId,
                this.name,
                this.description,
                this.createdAt,
                this.updatedAt
        );
    }

    public static RoleEntity fromDomain(com.safetrip.backend.domain.model.Role role) {
        if (role == null) {
            return null;
        }
        return RoleEntity.builder()
                .roleId(role.getRoleId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}