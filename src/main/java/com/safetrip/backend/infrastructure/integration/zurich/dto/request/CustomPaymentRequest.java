package com.safetrip.backend.infrastructure.integration.zurich.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPaymentRequest {

    private String amount;

    @JsonProperty("plan_name")
    private String planName;

    private String currency;

    @JsonProperty("hotel_name")
    private String hotelName;

    private String nit;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("confirmation_url")
    private String confirmationUrl;

    @JsonProperty("response_url")
    private String responseUrl;
}