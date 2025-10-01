package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.persistence.entity.UserEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.UserMapper;
import com.safetrip.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userJpaRepository.findByPhone(phone)
                .map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        return UserMapper.toDomain(userJpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }
}