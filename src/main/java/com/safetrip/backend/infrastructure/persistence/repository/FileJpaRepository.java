package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileJpaRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByFileName(String fileName);
}