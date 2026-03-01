package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.request.PurchaseTimePlanRequest;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;

public interface WalletTimePlanPurchaseService {
    PurchasePlanResponse purchaseTimePlan(PurchaseTimePlanRequest request);
    void confirmTimePlanPayment(String voucher, String statusCard) throws RuntimeException;
}