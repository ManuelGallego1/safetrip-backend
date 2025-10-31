package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.PolicyFile;
import java.util.List;
import java.util.Optional;

public interface PolicyFileRepository {
    PolicyFile save(PolicyFile policyFile);
    Optional<PolicyFile> findById(Long policyFileId);
    List<PolicyFile> findAll();
    void delete(Long policyFileId);
}