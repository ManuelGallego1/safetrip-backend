package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear o actualizar una persona
 * con un documento que ya existe en el sistema
 */
public class PersonAlreadyExistsException extends DomainException {

    private final String documentNumber;

    public PersonAlreadyExistsException(String documentNumber) {
        super(String.format("Ya existe una persona con el número de documento: %s", documentNumber));
        this.documentNumber = documentNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}