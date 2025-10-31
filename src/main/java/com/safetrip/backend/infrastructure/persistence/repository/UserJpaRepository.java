package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.person WHERE u.email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);
    Optional<UserEntity> findByPhone(String phone);
}
