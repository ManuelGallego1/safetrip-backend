package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Role;
import com.safetrip.backend.domain.repository.RoleRepository;
import com.safetrip.backend.infrastructure.persistence.entity.RoleEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.RoleMapper;
import com.safetrip.backend.infrastructure.persistence.repository.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    public RoleRepositoryImpl(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = RoleMapper.toEntity(role);
        return RoleMapper.toDomain(roleJpaRepository.save(entity));
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleJpaRepository.findById(id)
                .map(RoleMapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(RoleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll()
                .stream()
                .map(RoleMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        roleJpaRepository.deleteById(id);
    }
}