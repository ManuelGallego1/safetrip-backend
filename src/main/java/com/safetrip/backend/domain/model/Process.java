package com.safetrip.backend.domain.model;

import java.time.ZonedDateTime;

public class Process {

    private final Long processId;
    private final Parameter parameter;
    private final String description;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;

    public Process(Long processId,
                   Parameter parameter,
                   String description,
                   ZonedDateTime createdAt,
                   ZonedDateTime updatedAt) {
        this.processId = processId;
        this.parameter = parameter;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getProcessId() {
        return processId;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}