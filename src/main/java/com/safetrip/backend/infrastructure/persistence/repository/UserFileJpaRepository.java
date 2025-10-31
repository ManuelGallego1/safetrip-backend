package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.UserFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFileJpaRepository extends JpaRepository<UserFileEntity, Long> {
}