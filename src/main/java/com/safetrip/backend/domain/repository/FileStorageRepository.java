package com.safetrip.backend.domain.repository;

public interface FileStorageRepository {
    String upload(String fileName, String contentType, byte[] data);
    byte[] download(String fileName);
    void delete(String fileName);
}