package com.novawavex.novawavex.workflow;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =========================================
     * WORKFLOW
     * =========================================
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "workflow_id",
            nullable = false
    )
    private Workflow workflow;

    /*
     * =========================================
     * EXECUTION STATUS
     * =========================================
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ExecutionStatus status;

    /*
     * =========================================
     * EXECUTION TIMESTAMPS
     * =========================================
     */
    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    /*
     * =========================================
     * CREATION INFORMATION
     * =========================================
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 150)
    private String createdBy;

    /*
     * =========================================
     * AUDIT INFORMATION
     * =========================================
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 150)
    private String updatedBy;

    /*
     * =========================================
     * FAILURE INFORMATION
     * =========================================
     */
    @Column(length = 2000)
    private String errorMessage;

    /*
     * =========================================
     * CONSTRUCTORS
     * =========================================
     */
    public WorkflowExecution() {
    }

    public WorkflowExecution(
            Workflow workflow,
            String createdBy) {

        this.workflow = workflow;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;

        this.status = ExecutionStatus.QUEUED;

        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    /*
     * =========================================
     * JPA LIFECYCLE
     * =========================================
     */
    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = ExecutionStatus.QUEUED;
        }

        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    public void onUpdate() {

        updatedAt = LocalDateTime.now();

        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    /*
     * =========================================
     * GETTERS
     * =========================================
     */
    public Long getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /*
     * =========================================
     * SETTERS
     * =========================================
     */
    public void setWorkflow(
            Workflow workflow) {

        this.workflow = workflow;
    }

    public void setStatus(
            ExecutionStatus status) {

        this.status = status;
    }

    public void setStartedAt(
            LocalDateTime startedAt) {

        this.startedAt = startedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }

    public void setCreatedBy(
            String createdBy) {

        this.createdBy = createdBy;
    }

    public void setUpdatedBy(
            String updatedBy) {

        this.updatedBy = updatedBy;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage = errorMessage;
    }
}