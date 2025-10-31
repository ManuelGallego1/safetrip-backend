package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.File;
import com.safetrip.backend.domain.repository.FileRepository;
import com.safetrip.backend.infrastructure.persistence.entity.FileEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.FileMapper;
import com.safetrip.backend.infrastructure.persistence.repository.FileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FileRepositoryImpl implements FileRepository {

    private final FileJpaRepository fileJpaRepository;

    public FileRepositoryImpl(FileJpaRepository fileJpaRepository) {
        this.fileJpaRepository = fileJpaRepository;
    }

    @Override
    public File save(File file) {
        FileEntity entity = FileMapper.toEntity(file);
        FileEntity saved = fileJpaRepository.save(entity);
        return FileMapper.toDomain(saved);
    }

    @Override
    public Optional<File> findById(Long fileId) {
        return fileJpaRepository.findById(fileId)
                .map(FileMapper::toDomain);
    }

    @Override
    public List<File> findAll() {
        return fileJpaRepository.findAll().stream()
                .map(FileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long fileId) {
        fileJpaRepository.deleteById(fileId);
    }
}