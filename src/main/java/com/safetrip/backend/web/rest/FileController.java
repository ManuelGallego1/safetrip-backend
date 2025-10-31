package com.safetrip.backend.web.rest;

import com.safetrip.backend.web.dto.mapper.FileResponseMapper;
import com.safetrip.backend.application.service.FileAppService;
import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.FileResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileAppService fileAppService;

    public FileController(FileAppService fileAppService) {
        this.fileAppService = fileAppService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<FileResponse>> uploadUserFile(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws Exception {

        File savedFile = fileAppService.uploadFileForUser(userId, file);
        FileResponse response = FileResponseMapper.toDto(savedFile);

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully for user", response));
    }

    @PostMapping("/policy/{policyId}")
    public ResponseEntity<ApiResponse<FileResponse>> uploadPolicyFile(
            @PathVariable Long policyId,
            @RequestParam("file") MultipartFile file) throws Exception {

        File savedFile = fileAppService.uploadFileForPolicy(policyId, file);
        FileResponse response = FileResponseMapper.toDto(savedFile);

        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully for policy", response));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        byte[] data = fileAppService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + fileId + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<String>> deleteFile(@PathVariable Long fileId) {
        fileAppService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", "File ID: " + fileId));
    }
}