package com.safetrip.backend.infrastructure.persistence.mapper;

import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;

public class PersonMapper {

    private PersonMapper() {
    }

    public static Person toDomain(PersonEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Person(
                entity.getPersonId(),
                entity.getFullName(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PersonEntity toEntity(Person person) {
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
