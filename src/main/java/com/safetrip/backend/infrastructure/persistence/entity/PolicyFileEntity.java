package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "policy_files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_file_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyFileId;

    @Column(name = "policy_id", nullable = false)
    @ToString.Include
    private Long policyId;

    @Column(name = "file_id", nullable = false)
    @ToString.Include
    private Long fileId;
}