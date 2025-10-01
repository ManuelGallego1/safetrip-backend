package com.safetrip.backend.domain.repository;

import com.safetrip.backend.domain.model.Discount;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository {

    Discount save(Discount discount);

    Optional<Discount> findById(Long discountId);

    Optional<Discount> findByName(String name);

    List<Discount> findAll();

    void delete(Long discountId);
}