package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.repository.DiscountRepository;
import com.safetrip.backend.infrastructure.persistence.entity.DiscountEntity;
import com.safetrip.backend.infrastructure.persistence.mapper.DiscountMapper;
import com.safetrip.backend.infrastructure.persistence.repository.DiscountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DiscountRepositoryImpl implements DiscountRepository {

    private final DiscountJpaRepository discountJpaRepository;

    public DiscountRepositoryImpl(DiscountJpaRepository discountJpaRepository) {
        this.discountJpaRepository = discountJpaRepository;
    }

    @Override
    public Discount save(Discount discount) {
        DiscountEntity entity = DiscountMapper.toEntity(discount);
        DiscountEntity saved = discountJpaRepository.save(entity);
        return DiscountMapper.toDomain(saved);
    }

    @Override
    public Optional<Discount> findById(Long discountId) {
        return discountJpaRepository.findById(discountId)
                .map(DiscountMapper::toDomain);
    }

    @Override
    public Optional<Discount> findByName(String name) {
        return discountJpaRepository.findByName(name)
                .map(DiscountMapper::toDomain);
    }

    @Override
    public List<Discount> findAll() {
        return discountJpaRepository.findAll().stream()
                .map(DiscountMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long discountId) {
        discountJpaRepository.deleteById(discountId);
    }
}