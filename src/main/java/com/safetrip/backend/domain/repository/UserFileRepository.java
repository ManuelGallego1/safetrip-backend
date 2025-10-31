package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.UserFile;
import java.util.List;
import java.util.Optional;

public interface UserFileRepository {
    UserFile save(UserFile userFile);
    Optional<UserFile> findById(Long userFileId);
    List<UserFile> findAll();
    void delete(Long userFileId);
}