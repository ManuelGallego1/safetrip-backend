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
@Table(name = "persons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"document_type", "document_number"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long personId;

    @Column(name = "full_name", nullable = false, length = 250)
    private String fullName;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 100)
    private String documentNumber;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserEntity> users;

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
                .createdAt(person.getCreatedAt())
                .updatedAt(person.getUpdatedAt())
                .build();
    }
}
