package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class Policy {

    private final Long policyId;
    private final PolicyType policyType;
    private final Integer personCount;
    private final BigDecimal unitPriceWithDiscount;
    private final Discount discount;
    private final String policyNumber;
    private final User createdByUser;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Policy(Long policyId,
                  PolicyType policyType,
                  Integer personCount,
                  BigDecimal unitPriceWithDiscount,
                  Discount discount,
                  String policyNumber,
                  User createdByUser,
                  ZonedDateTime createdAt,
                  ZonedDateTime updatedAt,
                  List<PolicyDetail> policyDetails,
                  List<PolicyPayment> policyPayments) {
        this.policyId = policyId;
        this.policyType = policyType;
        this.personCount = personCount != null ? personCount : 1;
        this.unitPriceWithDiscount = unitPriceWithDiscount != null ? unitPriceWithDiscount : BigDecimal.ZERO;
        this.discount = discount;
        this.policyNumber = policyNumber;
        this.createdByUser = createdByUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public Integer getPersonCount() {
        return personCount;
    }

    public BigDecimal getUnitPriceWithDiscount() {
        return unitPriceWithDiscount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}