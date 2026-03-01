package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.domain.model.enums.DiscountType;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository {
    Discount save(Discount discount);
    Optional<Discount> findById(Long id);
    Optional<Discount> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndDiscountIdNot(String name, Long discountId);
    void deleteById(Long id);
    List<Discount> findAllActive();
    List<Discount> findByType(DiscountType type);
}