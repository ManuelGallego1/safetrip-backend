package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.infrastructure.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonJpaRepository extends JpaRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
}