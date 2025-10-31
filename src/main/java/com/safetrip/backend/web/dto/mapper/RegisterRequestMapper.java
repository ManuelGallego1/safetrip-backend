package com.safetrip.backend.web.dto.mapper;

import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.Role;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.domain.repository.RoleRepository;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class RegisterRequestMapper {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;

    public RegisterRequestMapper(RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 PersonRepository personRepository) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
    }

    public User toDomain(RegisterRequest dto) {
        Optional<Person> existingPerson = personRepository.findByDocument(
                dto.getPerson().getDocumentType(),
                dto.getPerson().getDocumentNumber()
        );

        Person person = existingPerson.orElseGet(() -> new Person(
                null,
                dto.getPerson().getFullName(),
                dto.getPerson().getDocumentType(),
                dto.getPerson().getDocumentNumber(),
                dto.getPerson().getAddress(),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        ));

        Optional<Role> role = roleRepository.findById(2L);

        return new User(
                null,
                person,
                dto.getEmail(),
                dto.getPhone(),
                passwordEncoder.encode(dto.getPassword()),
                role.orElse(null),
                false,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                null
        );
    }
}