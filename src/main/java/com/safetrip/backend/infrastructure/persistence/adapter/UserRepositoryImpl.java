package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.persistence.entity.UserEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.UserMapper;
import com.safetrip.backend.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);

        // Si el usuario ya existe (tiene ID), recuperar la entidad existente
        // y actualizar sus campos en lugar de crear una nueva
        if (user.getUserId() != null) {
            UserEntity existingEntity = userJpaRepository.findById(user.getUserId())
                    .orElse(null);

            if (existingEntity != null) {
                // Actualizar campos de la entidad existente
                existingEntity.setEmail(entity.getEmail());
                existingEntity.setPhone(entity.getPhone());
                existingEntity.setPasswordHash(entity.getPasswordHash());
                existingEntity.setRole(entity.getRole());
                existingEntity.setIsActive(entity.getIsActive());
                existingEntity.setUpdatedAt(entity.getUpdatedAt());
                existingEntity.setProfileImageUrl(entity.getProfileImageUrl());

                // ⚠️ NO actualizar Person aquí, ya fue actualizado antes en el service
                // Mantener la referencia existente
                existingEntity.setPerson(entity.getPerson());

                entity = existingEntity;
            }
        }

        UserEntity savedEntity = userJpaRepository.save(entity);
        return UserMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }
}