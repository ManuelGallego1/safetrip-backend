package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.service.ParameterService;
import com.safetrip.backend.application.service.ProcessService;
import com.safetrip.backend.domain.model.Parameter;
import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.domain.repository.ProcessRepository;
import com.safetrip.backend.infrastructure.security.EncryptionService;
import com.safetrip.backend.web.dto.request.ProcessRequest;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final EncryptionService encryptionService;
    private final ParameterService parameterService;

    public ProcessServiceImpl(ProcessRepository processRepository,
                              EncryptionService encryptionService, ParameterService parameterService) {
        this.processRepository = processRepository;
        this.encryptionService = encryptionService;
        this.parameterService = parameterService;
    }

    @Override
    public Process createProcess(ProcessRequest process) {
        Parameter parameter = parameterService.getParameterById(process.getParameterId())
                .orElseThrow(() -> new RuntimeException("Parameter not found"));
        try {
            String encryptedValue = encryptionService.encrypt(process.getValue());
            Process p = new Process(
                    null,
                    parameter,
                    process.getDescription(),
                    ZonedDateTime.now(),
                    ZonedDateTime.now(),
                    encryptedValue
            );
            return processRepository.save(p);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando value", e);
        }
    }

    @Override
    public Optional<Process> getProcessById(Long processId) {
        return processRepository.findById(processId);
    }

    @Override
    public List<Process> getAllProcesses() {
        return processRepository.findAll();
    }

    @Override
    public Process updateProcess(Process process) {
        try {
            String encryptedValue = encryptionService.encrypt(process.getValue());
            Process p = new Process(
                    process.getProcessId(),
                    process.getParameter(),
                    process.getDescription(),
                    process.getCreatedAt(),
                    ZonedDateTime.now(),
                    encryptedValue
            );
            return processRepository.save(p);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando value", e);
        }
    }

    @Override
    public void deleteProcess(Long processId) {
        processRepository.deleteById(processId);
    }

    @Override
    public String getDecryptedValue(Process process) {
        try {
            return encryptionService.decrypt(process.getValue());
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando value", e);
        }
    }
}