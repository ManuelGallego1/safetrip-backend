package com.safetrip.backend.application.service;

import com.safetrip.backend.web.dto.response.WalletBalanceResponse;
import com.safetrip.backend.web.dto.response.WalletConsumptionResponse;

import java.util.List;

public interface WalletService {

    /**
     * Obtiene todas las wallets del usuario autenticado con su balance y consumo
     */
    List<WalletBalanceResponse> getUserWalletBalances();

    /**
     * Obtiene el detalle de consumo de una wallet específica
     */
    WalletConsumptionResponse getWalletConsumption(Long walletId);
}