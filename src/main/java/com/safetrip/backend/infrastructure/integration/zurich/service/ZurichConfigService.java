package com.safetrip.backend.infrastructure.integration.zurich;

import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.domain.repository.ProcessRepository;
import com.safetrip.backend.infrastructure.security.EncryptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZurichConfigService {

    private final ProcessRepository processRepository;
    private final EncryptionService encryptionService;

    public ZurichConfigService(ProcessRepository processRepository,
                               EncryptionService encryptionService) {
        this.processRepository = processRepository;
        this.encryptionService = encryptionService;
    }

    public ZurichIntegrationConfig getZurichConfig(Long parameterId) {
        List<Process> processes = processRepository.findByParameterId(parameterId);

        String username = null;
        String password = null;
        String csrfToken = null;
        String baseUrl = null;

        for (Process process : processes) {
            String decryptedValue = encryptionService.decrypt(process.getValue());
            switch (process.getDescription()) {
                case "username" -> username = decryptedValue;
                case "password" -> password = decryptedValue;
                case "csrfToken" -> csrfToken = decryptedValue;
                case "baseUrl" -> baseUrl = decryptedValue;
            }
        }

        if (username == null || password == null || csrfToken == null || baseUrl == null) {
            throw new RuntimeException("Faltan datos de configuración para Zurich");
        }

        return new ZurichIntegrationConfig(username, password, csrfToken, baseUrl);
    }
}