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

        // Actualizar email si viene en el request
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            uniquenessValidator.validateEmailUniqueness(request.getEmail(), userId);
            user = user.updateEmail(request.getEmail());
        }

        // Actualizar teléfono si viene en el request
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            uniquenessValidator.validatePhoneUniqueness(request.getPhoneNumber(), userId);
            user = user.updatePhone(request.getPhoneNumber());
        }

        // Actualizar Person si existe y hay cambios
        if (user.getPerson() != null) {
            Person person = user.getPerson();
            boolean personChanged = false;

            if (request.getFullName() != null && !request.getFullName().isBlank()) {
                person = person.updateFullName(request.getFullName());
                personChanged = true;
            }

            if (request.getAddress() != null && !request.getAddress().isBlank()) {
                person = person.updateAddress(request.getAddress());
                personChanged = true;
            }

            if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()) {
                uniquenessValidator.validateDocumentUniqueness(
                        person.getDocumentType(),
                        request.getDocumentNumber(),
                        person.getPersonId()
                );
                person = person.updateDocumentNumber(request.getDocumentNumber());
                personChanged = true;
            }

            // Solo guardar si hubo cambios
            if (personChanged) {
                person = personRepository.save(person);
                user = user.updatePerson(person);
            }
        }

        // Guardar y retornar usuario actualizado
        return userRepository.save(user);
    }

    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}