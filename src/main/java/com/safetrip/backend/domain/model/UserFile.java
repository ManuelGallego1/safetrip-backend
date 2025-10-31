package com.safetrip.backend.domain.model;

public class UserFile {
    private final Long userFileId;
    private final Long userId;
    private final Long fileId;

    public UserFile(Long userFileId, Long userId, Long fileId) {
        this.userFileId = userFileId;
        this.userId = userId;
        this.fileId = fileId;
    }

    public Long getUserFileId() { return userFileId; }
    public Long getUserId() { return userId; }
    public Long getFileId() { return fileId; }
}