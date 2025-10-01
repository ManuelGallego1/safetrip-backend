package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    User save(User user);

    Optional<User> findById(Long id);
}