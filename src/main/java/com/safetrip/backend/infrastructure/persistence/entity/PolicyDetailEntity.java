package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "policy_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PolicyDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_detail_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long policyDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_fk", nullable = false)
    @JsonIgnore  // Cambiado de @JsonBackReference
    @ToString.Exclude  // CRÍTICO: Excluir para evitar el bucle infinito
    private PolicyEntity policy;

    @Column(name = "origin", nullable = false, length = 250)
    @ToString.Include
    private String origin;

    @Column(name = "destination", length = 250)
    @ToString.Include
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