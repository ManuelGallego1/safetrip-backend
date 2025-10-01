package com.safetrip.backend.domain.model;

import com.safetrip.backend.domain.model.enums.RelationshipType;

import java.time.ZonedDateTime;

public class PolicyPerson {

    private final Long policyPersonId;
    private final Policy policy;
    private final Person person;
    private final RelationshipType relationship;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public PolicyPerson(Long policyPersonId,
                        Policy policy,
                        Person person,
                        RelationshipType relationship,
                        ZonedDateTime createdAt,
                        ZonedDateTime updatedAt) {
        this.policyPersonId = policyPersonId;
        this.policy = policy;
        this.person = person;
        this.relationship = relationship;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getPolicyPersonId() {
        return policyPersonId;
    }

    public Policy getPolicy() {
        return policy;
    }

    public Person getPerson() {
        return person;
    }

    public RelationshipType getRelationship() {
        return relationship;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}