package com.safetrip.backend.application.service;

import com.safetrip.backend.application.dto.PaymentDTO;
import com.safetrip.backend.web.dto.request.ConfirmPaymentRequest;
import com.safetrip.backend.web.dto.response.PolicyResponse;

public interface PaymentService {
    String createdPaymentWithZurich (PaymentDTO paymentDTO);
    String cretaedPaymentWithWallet (PaymentDTO paymentDTO);
    PolicyResponse confirmPayment(ConfirmPaymentRequest confirmPaymentRequest);
}
