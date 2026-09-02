package com.novawavex.novawavex.dto;

import com.novawavex.novawavex.workflow.WorkflowStatus;

import java.time.LocalDateTime;

public class WorkflowResponse {

    private Long id;
    private String name;
    private String description;
    private WorkflowStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkflowResponse() {
    }

    public WorkflowResponse(
            Long id,
            String name,
            String description,
            WorkflowStatus status,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}