package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.ProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessJpaRepository extends JpaRepository<ProcessEntity, Long> {

    List<ProcessEntity> findByParameter_ParameterId(Long parameterId);
}