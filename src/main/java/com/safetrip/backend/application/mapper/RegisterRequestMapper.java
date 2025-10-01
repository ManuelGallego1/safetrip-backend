package com.safetrip.backend.application.mapper;

import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.Role;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.enums.RoleName;
import com.safetrip.backend.domain.repository.RoleRepository;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;

@Component
public class RegisterMapper {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterMapper(RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User toDomain(RegisterRequest dto) {
        Person person = new Person(
                null,
                dto.getFullName(),
                dto.getDocumentType(),
                dto.getDocumentNumber(),
                dto.getAddress(),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        Role role = getRoleOrThrow(dto.getRole());

        return new User(
                null,
                person,
                dto.getEmail(),
                dto.getPhone(),
                passwordEncoder.encode(dto.getPassword()),
                role,
                true,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }

    private Role getRoleOrThrow(String role) {
        RoleName roleName = RoleName.fromValue(role);
        return roleRepository.findByName(roleName.getValue())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Role not found: " + roleName.getValue()
                ));
    }
}