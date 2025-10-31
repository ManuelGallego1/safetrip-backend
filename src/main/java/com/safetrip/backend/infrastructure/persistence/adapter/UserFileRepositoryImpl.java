package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.UserFile;
import com.safetrip.backend.domain.repository.UserFileRepository;
import com.safetrip.backend.infrastructure.persistence.entity.UserFileEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.UserFileMapper;
import com.safetrip.backend.infrastructure.persistence.repository.UserFileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserFileRepositoryImpl implements UserFileRepository {

    private final UserFileJpaRepository userFileJpaRepository;

    public UserFileRepositoryImpl(UserFileJpaRepository userFileJpaRepository) {
        this.userFileJpaRepository = userFileJpaRepository;
    }

    @Override
    public UserFile save(UserFile userFile) {
        UserFileEntity entity = UserFileMapper.toEntity(userFile);
        return UserFileMapper.toDomain(userFileJpaRepository.save(entity));
    }

    @Override
    public Optional<UserFile> findById(Long userFileId) {
        return userFileJpaRepository.findById(userFileId)
                .map(UserFileMapper::toDomain);
    }

    @Override
    public List<UserFile> findAll() {
        return userFileJpaRepository.findAll().stream()
                .map(UserFileMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userFileId) {
        userFileJpaRepository.deleteById(userFileId);
    }
}