package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Parameter;

import java.util.List;
import java.util.Optional;

public interface ParameterRepository {

    Parameter save(Parameter parameter);

    Optional<Parameter> findById(Long parameterId);

    List<Parameter> findAll();

    void delete(Long parameterId);
}