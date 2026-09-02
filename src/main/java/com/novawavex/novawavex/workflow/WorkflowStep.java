package com.novawavex.novawavex.workflow;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {

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
     * STEP INFORMATION
     * =========================================
     */

    @Column(
        nullable = false,
        length = 150
    )
    private String name;

    @Column(length = 1000)
    private String description;


    /*
     * =========================================
     * STEP ORDER
     * =========================================
     */

    @Column(nullable = false)
    private Integer stepOrder;


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

    public WorkflowStep() {
    }


    public WorkflowStep(
            Workflow workflow,
            String name,
            String description,
            Integer stepOrder) {

        this.workflow = workflow;
        this.name = name;
        this.description = description;
        this.stepOrder = stepOrder;
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getStepOrder() {
        return stepOrder;
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

    public void setName(
            String name) {

        this.name = name;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public void setStepOrder(
            Integer stepOrder) {

        this.stepOrder = stepOrder;
    }
}