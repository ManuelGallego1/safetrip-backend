package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.exception.ParameterNotFoundException;
import com.safetrip.backend.application.exception.ProcessNotFoundException;
import com.safetrip.backend.application.service.ParameterService;
import com.safetrip.backend.application.service.ProcessService;
import com.safetrip.backend.domain.model.Parameter;
import com.safetrip.backend.domain.model.Process;
import com.safetrip.backend.web.dto.request.ParameterRequest;
import com.safetrip.backend.web.dto.request.ProcessRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credentials")
public class CredentialsController {

    private final ParameterService parameterService;
    private final ProcessService processService;

    public CredentialsController(ParameterService parameterService,
                                 ProcessService processService) {
        this.parameterService = parameterService;
        this.processService = processService;
    }

    // -------- PARAMETERS --------

    @PostMapping("/parameters")
    public ResponseEntity<ApiResponse<Parameter>> createParameter(@RequestBody ParameterRequest description) {
        Parameter created = parameterService.createParameter(description.getDescription());
        return ResponseEntity.ok(ApiResponse.success("Parameter created successfully", created));
    }

    @GetMapping("/parameters")
    public ResponseEntity<ApiResponse<List<Parameter>>> getAllParameters() {
        List<Parameter> list = parameterService.getAllParameters();
        return ResponseEntity.ok(ApiResponse.success("Parameters retrieved successfully", list));
    }

    @GetMapping("/parameters/{id}")
    public ResponseEntity<ApiResponse<Parameter>> getParameterById(@PathVariable Long id) {
        Parameter param = parameterService.getParameterById(id)
                .orElseThrow(() -> new ParameterNotFoundException("Parameter not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Parameter retrieved successfully", param));
    }

    // -------- PROCESSES --------

    @PostMapping("/processes")
    public ResponseEntity<ApiResponse<Process>> createProcess(@RequestBody ProcessRequest process) {
        Process created = processService.createProcess(process);
        return ResponseEntity.ok(ApiResponse.success("Process created successfully", created));
    }

    @GetMapping("/processes")
    public ResponseEntity<ApiResponse<List<Process>>> getAllProcesses() {
        List<Process> list = processService.getAllProcesses();
        return ResponseEntity.ok(ApiResponse.success("Processes retrieved successfully", list));
    }

    @GetMapping("/processes/{id}")
    public ResponseEntity<ApiResponse<Process>> getProcessById(@PathVariable Long id) {
        Process proc = processService.getProcessById(id)
                .orElseThrow(() -> new ProcessNotFoundException("Process not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Process retrieved successfully", proc));
    }

    @GetMapping("/processes/{id}/decrypted")
    public ResponseEntity<ApiResponse<String>> getDecryptedValue(@PathVariable Long id) {
        Process proc = processService.getProcessById(id)
                .orElseThrow(() -> new ProcessNotFoundException("Process not found with id: " + id));

        String value = processService.getDecryptedValue(proc);
        return ResponseEntity.ok(ApiResponse.success("Value decrypted successfully", value));
    }
}