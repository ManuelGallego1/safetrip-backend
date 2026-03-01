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

    // Métodos de actualización inmutables que MANTIENEN el ID
    public Person updateFullName(String newFullName) {
        validateFullName(newFullName);
        return new Person(
                this.personId,  // ✅ Mantener ID
                newFullName,
                this.documentType,
                this.documentNumber,
                this.address,
                this.createdAt,
                ZonedDateTime.now()  // Actualizar timestamp
        );
    }

    public Person updateAddress(String newAddress) {
        validateAddress(newAddress);
        return new Person(
                this.personId,  // ✅ Mantener ID
                this.fullName,
                this.documentType,
                this.documentNumber,
                newAddress,
                this.createdAt,
                ZonedDateTime.now()  // Actualizar timestamp
        );
    }

    public Person updateDocumentNumber(String newDocumentNumber) {
        validateDocumentNumber(newDocumentNumber);
        return new Person(
                this.personId,  // ✅ Mantener ID
                this.fullName,
                this.documentType,
                newDocumentNumber,
                this.address,
                this.createdAt,
                ZonedDateTime.now()  // Actualizar timestamp
        );
    }

    public Person updateDocumentType(DocumentType newDocumentType) {
        if (newDocumentType == null) {
            throw new IllegalArgumentException("El tipo de documento no puede ser nulo");
        }
        return new Person(
                this.personId,  // ✅ Mantener ID
                this.fullName,
                newDocumentType,
                this.documentNumber,
                this.address,
                this.createdAt,
                ZonedDateTime.now()  // Actualizar timestamp
        );
    }

    // Validaciones de invariantes del dominio
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }
        if (fullName.length() > 255) {
            throw new IllegalArgumentException("El nombre completo no puede exceder 255 caracteres");
        }
    }

    private void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }
        if (address.length() > 500) {
            throw new IllegalArgumentException("La dirección no puede exceder 500 caracteres");
        }
    }

    private void validateDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        if (documentNumber.length() > 50) {
            throw new IllegalArgumentException("El número de documento no puede exceder 50 caracteres");
        }
    }
}