package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.WalletType;
import com.safetrip.backend.domain.repository.WalletTypeRepository;
import com.safetrip.backend.infrastructure.persistence.entity.WalletTypeEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.WalletTypeMapper;
import com.safetrip.backend.infrastructure.persistence.repository.WalletTypeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WalletTypeRepositoryImpl implements WalletTypeRepository {

    private final WalletTypeJpaRepository walletTypeJpaRepository;

    public WalletTypeRepositoryImpl(WalletTypeJpaRepository walletTypeJpaRepository) {
        this.walletTypeJpaRepository = walletTypeJpaRepository;
    }

    @Override
    public WalletType save(WalletType walletType) {
        WalletTypeEntity entity = WalletTypeMapper.toEntity(walletType);
        return WalletTypeMapper.toDomain(walletTypeJpaRepository.save(entity));
    }

    @Override
    public Optional<WalletType> findById(Long id) {
        return walletTypeJpaRepository.findById(id)
                .map(WalletTypeMapper::toDomain);
    }

    @Override
    public List<WalletType> findAll() {
        return walletTypeJpaRepository.findAll()
                .stream()
                .map(WalletTypeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        walletTypeJpaRepository.deleteById(id);
    }

    @Override
    public Optional<WalletType> findByName(String name) {
        return walletTypeJpaRepository.findByName(name)
                .map(WalletTypeMapper::toDomain);
    }
}