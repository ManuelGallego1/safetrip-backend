package com.safetrip.backend.domain.model;

public class PolicyFile {
    private final Long policyFileId;
    private final Long policyId;
    private final Long fileId;

    public PolicyFile(Long policyFileId, Long policyId, Long fileId) {
        this.policyFileId = policyFileId;
        this.policyId = policyId;
        this.fileId = fileId;
    }

    public Long getPolicyFileId() { return policyFileId; }
    public Long getPolicyId() { return policyId; }
    public Long getFileId() { return fileId; }
}