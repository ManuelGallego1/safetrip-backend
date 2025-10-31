package com.safetrip.backend.domain.model;

import com.safetrip.backend.domain.model.enums.DocumentType;

import java.time.LocalDate;
import java.time.ZoneId;
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

    // Métodos de negocio con validación
    public Person updateFullName(String newFullName) {
        validateFullName(newFullName);
        return new Person(
                this.personId,
                newFullName,
                this.documentType,
                this.documentNumber,
                this.address,
                this.createdAt,
                ZonedDateTime.now(ZoneId.systemDefault())
        );
    }

    public Person updateAddress(String newAddress) {
        validateAddress(newAddress);
        return new Person(
                this.personId,
                this.fullName,
                this.documentType,
                this.documentNumber,
                newAddress,
                this.createdAt,
                ZonedDateTime.now(ZoneId.systemDefault())
        );
    }

    public Person updateDocumentNumber(String newDocumentNumber) {
        validateDocumentNumber(newDocumentNumber);
        return new Person(
                this.personId,
                this.fullName,
                this.documentType,
                newDocumentNumber,
                this.address,
                this.createdAt,
                ZonedDateTime.now(ZoneId.systemDefault())
        );
    }

    // Validaciones de negocio (invariantes del dominio)
    private void validateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }
        if (fullName.length() < 3) {
            throw new IllegalArgumentException("El nombre completo debe tener al menos 3 caracteres");
        }
        if (fullName.length() > 200) {
            throw new IllegalArgumentException("El nombre completo no puede exceder 200 caracteres");
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
        if (!documentNumber.matches("^[0-9A-Z\\-]+$")) {
            throw new IllegalArgumentException("El número de documento contiene caracteres inválidos");
        }
    }

    public boolean hasDocument(DocumentType documentType, String documentNumber) {
        return this.documentType.equals(documentType)
                && this.documentNumber.equals(documentNumber);
    }
}