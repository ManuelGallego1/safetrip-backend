package com.safetrip.backend.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una persona en el sistema
 */
public class PersonNotFoundException extends DomainException {

    private final Long personId;

    public PersonNotFoundException(Long personId) {
        super(String.format("Persona no encontrada con ID: %s", personId));
        this.personId = personId;
    }

    public Long getPersonId() {
        return personId;
    }
}