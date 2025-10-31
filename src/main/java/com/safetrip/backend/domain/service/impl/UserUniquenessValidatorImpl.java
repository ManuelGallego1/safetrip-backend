package com.safetrip.backend.domain.service.impl;

import com.safetrip.backend.domain.exception.PersonAlreadyExistsException;
import com.safetrip.backend.domain.exception.UserAlreadyExistsException;
import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.domain.service.UserUniquenessValidator;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del servicio de dominio para validar unicidad de usuarios y personas
 */
@Service
public class UserUniquenessValidatorImpl implements UserUniquenessValidator {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    public UserUniquenessValidatorImpl(UserRepository userRepository, PersonRepository personRepository) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
    }

    @Override
    public void validateEmailUniqueness(String email, Long excludeUserId) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(excludeUserId)) {
            throw new UserAlreadyExistsException("email", email);
        }
    }

    @Override
    public void validatePhoneUniqueness(String phone, Long excludeUserId) {
        Optional<User> existingUser = userRepository.findByPhone(phone);
        if (existingUser.isPresent() && !existingUser.get().getUserId().equals(excludeUserId)) {
            throw new UserAlreadyExistsException("teléfono", phone);
        }
    }

    @Override
    public void validateDocumentUniqueness(DocumentType documentType, String documentNumber, Long excludePersonId) {
        Optional<Person> existingPerson = personRepository.findByDocument(documentType, documentNumber);
        if (existingPerson.isPresent() && !existingPerson.get().getPersonId().equals(excludePersonId)) {
            throw new PersonAlreadyExistsException(documentNumber);
        }
    }
}