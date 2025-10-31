package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.web.dto.request.ProcessRequest;

import java.util.List;
import java.util.Optional;

public interface ProcessService {

    Process createProcess(ProcessRequest process);
    Optional<Process> getProcessById(Long processId);
    List<Process> getAllProcesses();
    Process updateProcess(Process process);
    void deleteProcess(Long processId);
    String getDecryptedValue(Process process);
}