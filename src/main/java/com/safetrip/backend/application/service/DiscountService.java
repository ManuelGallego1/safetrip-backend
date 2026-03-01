package com.safetrip.backend.application.service;

import com.safetrip.backend.domain.model.Discount;
import com.safetrip.backend.web.dto.request.DiscountRequest;

public interface DiscountService {
    Discount createDiscount(DiscountRequest request);
    Discount findByName(String name);
    Discount findById(Long id);
    Discount updateDiscount(Long id, DiscountRequest request);
    void deleteDiscount(Long id);
}