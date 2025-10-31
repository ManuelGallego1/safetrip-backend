package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.ParameterService;
import com.safetrip.backend.domain.model.Parameter;
import com.safetrip.backend.domain.repository.ParameterRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ParameterServiceImpl implements ParameterService {

    private final ParameterRepository parameterRepository;

    public ParameterServiceImpl(ParameterRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    @Override
    public Parameter createParameter(String description) {
        Parameter p = new Parameter(
                null,
                description,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
        return parameterRepository.save(p);
    }

    @Override
    public Optional<Parameter> getParameterById(Long parameterId) {
        return parameterRepository.findById(parameterId);
    }

    @Override
    public List<Parameter> getAllParameters() {
        return parameterRepository.findAll();
    }

    @Override
    public Parameter updateParameter(Parameter parameter) {
        Parameter p = new Parameter(
                parameter.getParameterId(),
                parameter.getDescription(),
                parameter.getCreatedAt(),
                ZonedDateTime.now()
        );
        return parameterRepository.save(p);
    }
}