package com.safetrip.backend.domain.model;

import com.safetrip.backend.domain.model.enums.DocumentType;

import java.time.ZonedDateTime;

public class Person {

    private final Long personId;
    private final String fullName;
    private final DocumentType documentType;
    private final String documentNumber;
    private final String address;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Person(Long personId,
                  String fullName,
                  DocumentType documentType,
                  String documentNumber,
                  String address,
                  ZonedDateTime createdAt,
                  ZonedDateTime updatedAt) {
        this.personId = personId;
        this.fullName = fullName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getPersonId() {
        return personId;
    }

    public String getFullName() {
        return fullName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getAddress() {
        return address;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}