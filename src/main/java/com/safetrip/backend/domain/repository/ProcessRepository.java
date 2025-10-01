package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Process;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository {

    Process save(Process process);

    Optional<Process> findById(Long id);

    List<Process> findAll();

    void deleteById(Long id);

    List<Process> findByParameterId(Long parameterId);
}