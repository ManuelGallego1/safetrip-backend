package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Parameter;
import com.safetrip.backend.domain.repository.ParameterRepository;
import com.safetrip.backend.infrastructure.persistence.entity.ParameterEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.ParameterMapper;
import com.safetrip.backend.infrastructure.persistence.repository.ParameterJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ParameterRepositoryImpl implements ParameterRepository {

    private final ParameterJpaRepository parameterJpaRepository;

    public ParameterRepositoryImpl(ParameterJpaRepository parameterJpaRepository) {
        this.parameterJpaRepository = parameterJpaRepository;
    }

    @Override
    public Parameter save(Parameter parameter) {
        ParameterEntity entity = ParameterMapper.toEntity(parameter);
        return ParameterMapper.toDomain(parameterJpaRepository.save(entity));
    }

    @Override
    public Optional<Parameter> findById(Long parameterId) {
        return parameterJpaRepository.findById(parameterId)
                .map(ParameterMapper::toDomain);
    }

    @Override
    public List<Parameter> findAll() {
        return parameterJpaRepository.findAll()
                .stream()
                .map(ParameterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long parameterId) {
        parameterJpaRepository.deleteById(parameterId);
    }
}