package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class PolicyDetail {

    private final Long policyDetailId;
    private final Policy policy;
    private final String origin;
    private final String destination;
    private final ZonedDateTime departure;
    private final ZonedDateTime arrival;
    private final String notes;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PolicyDetail(Long policyDetailId,
                        Policy policy,
                        String origin,
                        String destination,
                        ZonedDateTime departure,
                        ZonedDateTime arrival,
                        String notes,
                        ZonedDateTime createdAt,
                        ZonedDateTime updatedAt) {
        this.policyDetailId = policyDetailId;
        this.policy = policy;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPolicyDetailId() {
        return policyDetailId;
    }

    public Policy getPolicy() {
        return policy;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public ZonedDateTime getDeparture() {
        return departure;
    }

    public ZonedDateTime getArrival() {
        return arrival;
    }

    public String getNotes() {
        return notes;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static PolicyDetail of(
            Policy policy,
            String origin,
            String destination,
            ZonedDateTime departure,
            ZonedDateTime arrival,
            String notes
    ) {
        return new PolicyDetail(
                null,
                policy,
                origin,
                destination,
                departure,
                arrival,
                notes,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }
}
