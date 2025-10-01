package com.safetrip.backend.infrastructure.persistence.entity;

import com.safetrip.backend.domain.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_persons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_policy_person", columnNames = {"policy_fk", "person_fk"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_person_id")
    private Long policyPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_fk", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_fk", nullable = false)
    private PersonEntity person;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 30)
    private RelationshipType relationship;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}