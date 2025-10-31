package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safetrip.backend.domain.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_persons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_policy_person", columnNames = {"policy_fk", "person_fk"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyPersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_person_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyPersonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_fk", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_fk", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private PersonEntity person;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 30)
    @ToString.Include
    private RelationshipType relationship;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}