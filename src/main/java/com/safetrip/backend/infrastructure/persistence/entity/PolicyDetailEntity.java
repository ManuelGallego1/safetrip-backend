package com.safetrip.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_detail_id")
    private Long policyDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_fk", nullable = false)
    private PolicyEntity policy;

    @Column(name = "origin", nullable = false, length = 250)
    private String origin;

    @Column(name = "destination", length = 250)
    private String destination;

    @Column(name = "departure", nullable = false)
    private ZonedDateTime departure;

    @Column(name = "arrival")
    private ZonedDateTime arrival;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}