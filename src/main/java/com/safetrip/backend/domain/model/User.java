package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    private final Long userId;
    private final Person person;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final Role role;
    private final Boolean isActive;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final String profileImageUrl;

    public User(Long userId,
                Person person,
                String email,
                String phone,
                String passwordHash,
                Role role,
                Boolean isActive,
                ZonedDateTime createdAt,
                ZonedDateTime updatedAt,
                String profileImageUrl) {
        this.userId = userId;
        this.person = person;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive != null ? isActive : Boolean.TRUE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Person getPerson() {
        return person;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Métodos de negocio
    public User activate() {
        return new User(
                this.userId,
                this.person,
                this.email,
                this.phone,
                this.passwordHash,
                this.role,
                true,
                this.createdAt,
                ZonedDateTime.now(),
                this.profileImageUrl
        );
    }

    public User updateEmail(String newEmail) {
        validateEmail(newEmail);
        return new User(
                this.userId,
                this.person,
                newEmail,
                this.phone,
                this.passwordHash,
                this.role,
                this.isActive,
                this.createdAt,
                ZonedDateTime.now(),
                this.profileImageUrl
        );
    }

    public User updatePhone(String newPhone) {
        validatePhone(newPhone);
        return new User(
                this.userId,
                this.person,
                this.email,
                newPhone,
                this.passwordHash,
                this.role,
                this.isActive,
                this.createdAt,
                ZonedDateTime.now(),
                this.profileImageUrl
        );
    }

    public User updatePerson(Person newPerson) {
        if (newPerson == null) {
            throw new IllegalArgumentException("La persona no puede ser nula");
        }
        return new User(
                this.userId,
                newPerson,
                this.email,
                this.phone,
                this.passwordHash,
                this.role,
                this.isActive,
                this.createdAt,
                ZonedDateTime.now(),
                this.profileImageUrl
        );
    }

    public User updateProfileImage(String newProfileImageUrl) {
        return new User(
                this.userId,
                this.person,
                this.email,
                this.phone,
                this.passwordHash,
                this.role,
                this.isActive,
                this.createdAt,
                ZonedDateTime.now(),
                newProfileImageUrl
        );
    }

    // Validaciones de invariantes del dominio
    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El formato del email es inválido");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("El email no puede exceder 255 caracteres");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("El formato del teléfono es inválido");
        }
    }

    // Métodos de consulta de dominio
    public boolean hasEmail(String email) {
        return this.email.equalsIgnoreCase(email);
    }

    public boolean hasPhone(String phone) {
        return this.phone.equals(phone);
    }
}