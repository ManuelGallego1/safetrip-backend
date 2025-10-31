package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.Parameter;

import java.util.List;
import java.util.Optional;

public interface ParameterService {
    Parameter createParameter(String description);
    Optional<Parameter> getParameterById(Long parameterId);
    List<Parameter> getAllParameters();
    Parameter updateParameter(Parameter parameter);
}