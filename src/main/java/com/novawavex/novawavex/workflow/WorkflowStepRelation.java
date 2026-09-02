package com.novawavex.novawavex.workflow;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "workflow_step_relations",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "source_step_id",
                "target_step_id"
            }
        )
    }
)
public class WorkflowStepRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * =========================================
     * PARENT WORKFLOW
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
     * SOURCE STEP
     * =========================================
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "source_step_id",
        nullable = false
    )
    private WorkflowStep sourceStep;


    /*
     * =========================================
     * TARGET STEP
     * =========================================
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "target_step_id",
        nullable = false
    )
    private WorkflowStep targetStep;


    /*
     * =========================================
     * TIMESTAMP
     * =========================================
     */

    @Column(nullable = false)
    private LocalDateTime createdAt;


    /*
     * =========================================
     * CONSTRUCTORS
     * =========================================
     */

    public WorkflowStepRelation() {
    }


    public WorkflowStepRelation(
            Workflow workflow,
            WorkflowStep sourceStep,
            WorkflowStep targetStep) {

        this.workflow = workflow;
        this.sourceStep = sourceStep;
        this.targetStep = targetStep;
        this.createdAt = LocalDateTime.now();
    }


    /*
     * =========================================
     * JPA LIFECYCLE
     * =========================================
     */

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
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

    public WorkflowStep getSourceStep() {
        return sourceStep;
    }

    public WorkflowStep getTargetStep() {
        return targetStep;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    public void setSourceStep(
            WorkflowStep sourceStep) {

        this.sourceStep = sourceStep;
    }

    public void setTargetStep(
            WorkflowStep targetStep) {

        this.targetStep = targetStep;
    }
}