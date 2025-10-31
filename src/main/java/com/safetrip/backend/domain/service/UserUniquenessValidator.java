package com.safetrip.backend.domain.service;

import com.safetrip.backend.domain.model.enums.DocumentType;

/**
 * Servicio de dominio para validar unicidad de usuarios y personas
 * (reglas de negocio que requieren acceso a repositorios)
 */
public interface UserUniquenessValidator {

    /**
     * Valida que el email no esté en uso por otro usuario
     * @param email Email a validar
     * @param excludeUserId ID del usuario a excluir de la validación
     * @throws com.safetrip.backend.domain.exception.UserAlreadyExistsException si el email ya existe
     */
    void validateEmailUniqueness(String email, Long excludeUserId);

    /**
     * Valida que el teléfono no esté en uso por otro usuario
     * @param phone Teléfono a validar
     * @param excludeUserId ID del usuario a excluir de la validación
     * @throws com.safetrip.backend.domain.exception.UserAlreadyExistsException si el teléfono ya existe
     */
    void validatePhoneUniqueness(String phone, Long excludeUserId);

    /**
     * Valida que el documento no esté en uso por otra persona
     * @param documentType Tipo de documento
     * @param documentNumber Número de documento a validar
     * @param excludePersonId ID de la persona a excluir de la validación
     * @throws com.safetrip.backend.domain.exception.PersonAlreadyExistsException si el documento ya existe
     */
    void validateDocumentUniqueness(DocumentType documentType, String documentNumber, Long excludePersonId);
}