package com.safetrip.backend.web.rest;

import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.Wallet;
import com.safetrip.backend.web.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @GetMapping("/wallets")
    public ResponseEntity<ApiResponse<List<Wallet>>> getAllWallets() {
        return null;
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Payment>> getPaymentDetails() {
        return null;
    }
}
