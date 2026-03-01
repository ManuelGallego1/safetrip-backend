package com.safetrip.backend.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class PolicyPlan {

    private final Long policyPlanId;
    private final PolicyType policyType;
    private final Integer pax;
    private final BigDecimal discountPercentage;
    private final String description;
    private final Boolean popular;
    private final Boolean active;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PolicyPlan(Long policyPlanId,
                      PolicyType policyType,
                      Integer pax,
                      BigDecimal discountPercentage,
                      String description,
                      Boolean popular,
                      Boolean active,
                      ZonedDateTime createdAt,
                      ZonedDateTime updatedAt) {
        this.policyPlanId = policyPlanId;
        this.policyType = policyType;
        this.pax = pax != null ? pax : 1;
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        this.description = description;
        this.popular = popular != null ? popular : false;
        this.active = active != null ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PolicyPlan(Long policyPlanId) {
        this.policyPlanId = policyPlanId;
        this.policyType = null;
        this.pax = null;
        this.discountPercentage = null;
        this.description = null;
        this.popular = null;
        this.active = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    public Long getPolicyPlanId() {
        return policyPlanId;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public Integer getPax() {
        return pax;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getPopular() {
        return popular;
    }

    public Boolean getActive() {
        return active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}