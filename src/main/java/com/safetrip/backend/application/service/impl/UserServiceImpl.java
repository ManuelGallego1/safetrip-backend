package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.UserService;
import com.safetrip.backend.domain.exception.UserNotFoundException;
import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.domain.service.UserUniquenessValidator;
import com.safetrip.backend.web.dto.request.UserRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserUniquenessValidator uniquenessValidator;

    public UserServiceImpl(UserRepository userRepository,
                           PersonRepository personRepository,
                           UserUniquenessValidator uniquenessValidator) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.uniquenessValidator = uniquenessValidator;
    }

    @Override
    @Transactional
    public User updateUser(UserRequest request) {
        // Obtener usuario autenticado
        User authenticatedUser = getAuthenticatedUser();
        Long userId = authenticatedUser.getUserId();

        // Recargar usuario desde BD para asegurar consistencia
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // PASO 1: Actualizar Person primero si existe y hay cambios
        Person updatedPerson = null;
        if (user.getPerson() != null) {
            Person person = user.getPerson();
            boolean personChanged = false;

            // Actualizar nombre si viene y es diferente
            if (request.getFullName() != null && !request.getFullName().isBlank()
                    && !request.getFullName().equals(person.getFullName())) {
                person = person.updateFullName(request.getFullName());
                personChanged = true;
            }

            // Actualizar dirección si viene y es diferente
            if (request.getAddress() != null && !request.getAddress().isBlank()
                    && !request.getAddress().equals(person.getAddress())) {
                person = person.updateAddress(request.getAddress());
                personChanged = true;
            }

            // Actualizar documento si viene y es diferente
            if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()
                    && !request.getDocumentNumber().equals(person.getDocumentNumber())) {
                uniquenessValidator.validateDocumentUniqueness(
                        person.getDocumentType(),
                        request.getDocumentNumber(),
                        person.getPersonId()
                );
                person = person.updateDocumentNumber(request.getDocumentNumber());
                personChanged = true;
            }

            // Guardar Person PRIMERO si hubo cambios
            if (personChanged) {
                updatedPerson = personRepository.save(person);
            }
        }

        // PASO 2: Actualizar campos de User
        // Si Person cambió, actualizar la referencia
        if (updatedPerson != null) {
            user = user.updatePerson(updatedPerson);
        }

        // Actualizar email si viene y es diferente
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            uniquenessValidator.validateEmailUniqueness(request.getEmail(), userId);
            user = user.updateEmail(request.getEmail());
        }

        // Actualizar teléfono si viene y es diferente
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPhone())) {
            uniquenessValidator.validatePhoneUniqueness(request.getPhoneNumber(), userId);
            user = user.updatePhone(request.getPhoneNumber());
        }

        // PASO 3: Guardar User al final
        return userRepository.save(user);
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new UserNotFoundException("Usuario no autenticado o sesión inválida");
    }
}