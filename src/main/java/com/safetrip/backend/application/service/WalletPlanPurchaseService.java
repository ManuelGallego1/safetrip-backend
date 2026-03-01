package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.request.PurchasePlanRequest;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;

public interface WalletPlanPurchaseService {

    PurchasePlanResponse purchasePlan(PurchasePlanRequest request);
    void confirmPlanPayment(String voucher, String statusCard);
}