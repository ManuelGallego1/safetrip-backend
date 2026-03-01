package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.request.RechargeWalletRequest;
import com.safetrip.backend.web.dto.response.PurchasePlanResponse;

public interface WalletMoneyRechargeService {
    PurchasePlanResponse rechargeWallet(RechargeWalletRequest request);
    void confirmRechargePayment(String voucher, String statusCard);
}