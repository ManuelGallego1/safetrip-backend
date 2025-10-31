package com.safetrip.backend.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.safetrip.backend.domain.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"document_type", "document_number"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long personId;

    @Column(name = "full_name", nullable = false, length = 250)
    @ToString.Include
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    @ToString.Include
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    @ToString.Include
    private String documentNumber;

    @Column(name = "address", columnDefinition = "TEXT", nullable = true)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<UserEntity> users = new ArrayList<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<PolicyPersonEntity> policyPersons = new ArrayList<>();

    // Domain conversion methods
    public com.safetrip.backend.domain.model.Person toDomain() {
        return new com.safetrip.backend.domain.model.Person(
                this.personId,
                this.fullName,
                this.documentType,
                this.documentNumber,
                this.address,
                this.createdAt,
                this.updatedAt
        );
    }

    public static PersonEntity fromDomain(com.safetrip.backend.domain.model.Person person) {
        if (person == null) {
            return null;
        }
        return PersonEntity.builder()
                .personId(person.getPersonId())
                .fullName(person.getFullName())
                .documentType(person.getDocumentType())
                .documentNumber(person.getDocumentNumber())
                .address(person.getAddress())
                .build();
    }
}