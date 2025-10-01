package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.domain.repository.ProcessRepository;
import com.safetrip.backend.infrastructure.persistence.entity.ProcessEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.ProcessMapper;
import com.safetrip.backend.infrastructure.persistence.repository.ProcessJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProcessRepositoryImpl implements ProcessRepository {

    private final ProcessJpaRepository processJpaRepository;

    public ProcessRepositoryImpl(ProcessJpaRepository processJpaRepository) {
        this.processJpaRepository = processJpaRepository;
    }

    @Override
    public Process save(Process process) {
        ProcessEntity entity = ProcessMapper.toEntity(process);
        return ProcessMapper.toDomain(processJpaRepository.save(entity));
    }

    @Override
    public Optional<Process> findById(Long id) {
        return processJpaRepository.findById(id)
                .map(ProcessMapper::toDomain);
    }

    @Override
    public List<Process> findAll() {
        return processJpaRepository.findAll()
                .stream()
                .map(ProcessMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        processJpaRepository.deleteById(id);
    }

    @Override
    public List<Process> findByParameterId(Long parameterId) {
        return processJpaRepository.findByParameter_ParameterId(parameterId)
                .stream()
                .map(ProcessMapper::toDomain)
                .collect(Collectors.toList());
    }
}