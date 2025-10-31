package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.File;
import java.util.List;
import java.util.Optional;

public interface FileRepository {
    File save(File file);
    Optional<File> findById(Long fileId);
    List<File> findAll();
    void delete(Long fileId);
}