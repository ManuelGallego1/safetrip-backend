package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.model.enums.DiscountType;
import com.safetrip.backend.domain.repository.DiscountRepository;
import com.safetrip.backend.infrastructure.persistence.entity.DiscountEntity;
import com.safetrip.backend.infrastructure.persistence.repository.DiscountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DiscountRepositoryImpl implements DiscountRepository {

    private final DiscountJpaRepository jpaRepository;

    public DiscountRepositoryImpl(DiscountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Discount save(Discount discount) {
        DiscountEntity entity = DiscountEntity.fromDomain(discount);
        DiscountEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Discount> findById(Long id) {
        return jpaRepository.findById(id)
                .map(DiscountEntity::toDomain);
    }

    @Override
    public Optional<Discount> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(DiscountEntity::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndDiscountIdNot(String name, Long discountId) {
        return jpaRepository.existsByNameAndDiscountIdNot(name, discountId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Discount> findAllActive() {
        return jpaRepository.findAllByActiveTrue()
                .stream()
                .map(DiscountEntity::toDomain)
                .toList();
    }

    // ✅ NUEVO MÉTODO IMPLEMENTADO
    @Override
    public List<Discount> findByType(DiscountType type) {
        return jpaRepository.findByType(type)
                .stream()
                .map(DiscountEntity::toDomain)
                .toList();
    }
}