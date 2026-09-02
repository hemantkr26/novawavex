
package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowTest {

    private static final String WORKFLOW_NAME =
            "Test Workflow";

    private static final String DESCRIPTION =
            "Test workflow description";

    private static final String CREATED_BY =
            "userA@example.com";


    // =========================================================
    // 1. DEFAULT CONSTRUCTOR
    // =========================================================

    @Test
    void defaultConstructor_shouldCreateWorkflow() {

        Workflow workflow =
                new Workflow();

        assertNotNull(workflow);

        assertNull(workflow.getId());
        assertNull(workflow.getName());
        assertNull(workflow.getDescription());
        assertNull(workflow.getStatus());
        assertNull(workflow.getCreatedBy());
        assertNull(workflow.getCreatedAt());
        assertNull(workflow.getUpdatedAt());
    }


    // =========================================================
    // 2. PARAMETERIZED CONSTRUCTOR
    // =========================================================

    @Test
    void parameterizedConstructor_shouldInitializeWorkflow() {

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.DRAFT,
                        CREATED_BY
                );

        assertNull(workflow.getId());

        assertEquals(
                WORKFLOW_NAME,
                workflow.getName()
        );

        assertEquals(
                DESCRIPTION,
                workflow.getDescription()
        );

        assertEquals(
                WorkflowStatus.DRAFT,
                workflow.getStatus()
        );

        assertEquals(
                CREATED_BY,
                workflow.getCreatedBy()
        );

        assertNotNull(
                workflow.getCreatedAt()
        );

        assertNotNull(
                workflow.getUpdatedAt()
        );
    }


    // =========================================================
    // 3. CONSTRUCTOR TIMESTAMPS
    // =========================================================

    @Test
    void parameterizedConstructor_shouldSetCreationAndUpdateTimestamps() {

        LocalDateTime before =
                LocalDateTime.now();

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.DRAFT,
                        CREATED_BY
                );

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                workflow.getCreatedAt()
        );

        assertNotNull(
                workflow.getUpdatedAt()
        );

        assertFalse(
                workflow.getCreatedAt().isBefore(before)
        );

        assertFalse(
                workflow.getCreatedAt().isAfter(after)
        );

        assertFalse(
                workflow.getUpdatedAt().isBefore(before)
        );

        assertFalse(
                workflow.getUpdatedAt().isAfter(after)
        );
    }


    // =========================================================
    // 4. CREATED AND UPDATED TIMESTAMPS
    // =========================================================

    @Test
    void parameterizedConstructor_shouldInitializeBothTimestamps() {

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.ACTIVE,
                        CREATED_BY
                );

        assertNotNull(
                workflow.getCreatedAt()
        );

        assertNotNull(
                workflow.getUpdatedAt()
        );
    }


    // =========================================================
    // 5. SET NAME
    // =========================================================

    @Test
    void setName_shouldUpdateName() {

        Workflow workflow =
                new Workflow();

        workflow.setName(
                "Updated Workflow"
        );

        assertEquals(
                "Updated Workflow",
                workflow.getName()
        );
    }


    // =========================================================
    // 6. SET DESCRIPTION
    // =========================================================

    @Test
    void setDescription_shouldUpdateDescription() {

        Workflow workflow =
                new Workflow();

        workflow.setDescription(
                "Updated description"
        );

        assertEquals(
                "Updated description",
                workflow.getDescription()
        );
    }


    // =========================================================
    // 7. SET STATUS
    // =========================================================

    @Test
    void setStatus_shouldUpdateStatus() {

        Workflow workflow =
                new Workflow();

        workflow.setStatus(
                WorkflowStatus.ACTIVE
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                workflow.getStatus()
        );
    }


    // =========================================================
    // 8. SET CREATED BY
    // =========================================================

    @Test
    void setCreatedBy_shouldUpdateCreator() {

        Workflow workflow =
                new Workflow();

        workflow.setCreatedBy(
                "newuser@example.com"
        );

        assertEquals(
                "newuser@example.com",
                workflow.getCreatedBy()
        );
    }


    // =========================================================
    // 9. PRE-PERSIST WITH NULL TIMESTAMPS
    // =========================================================

    @Test
    void onCreate_shouldSetMissingTimestamps() {

        Workflow workflow =
                new Workflow();

        LocalDateTime before =
                LocalDateTime.now();

        workflow.onCreate();

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                workflow.getCreatedAt()
        );

        assertNotNull(
                workflow.getUpdatedAt()
        );

        assertFalse(
                workflow.getCreatedAt().isBefore(before)
        );

        assertFalse(
                workflow.getCreatedAt().isAfter(after)
        );

        assertFalse(
                workflow.getUpdatedAt().isBefore(before)
        );

        assertFalse(
                workflow.getUpdatedAt().isAfter(after)
        );
    }


    // =========================================================
    // 10. PRE-PERSIST SHOULD PRESERVE EXISTING TIMESTAMPS
    // =========================================================

    @Test
    void onCreate_shouldPreserveExistingTimestamps() {

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.DRAFT,
                        CREATED_BY
                );

        LocalDateTime originalCreatedAt =
                workflow.getCreatedAt();

        LocalDateTime originalUpdatedAt =
                workflow.getUpdatedAt();

        workflow.onCreate();

        assertEquals(
                originalCreatedAt,
                workflow.getCreatedAt()
        );

        assertEquals(
                originalUpdatedAt,
                workflow.getUpdatedAt()
        );
    }


    // =========================================================
    // 11. PRE-UPDATE
    // =========================================================

    @Test
    void onUpdate_shouldUpdateUpdatedAt() throws InterruptedException {

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.DRAFT,
                        CREATED_BY
                );

        LocalDateTime originalUpdatedAt =
                workflow.getUpdatedAt();

        Thread.sleep(2);

        workflow.onUpdate();

        assertNotNull(
                workflow.getUpdatedAt()
        );

        assertTrue(
                workflow.getUpdatedAt()
                        .isAfter(originalUpdatedAt)
        );
    }


    // =========================================================
    // 12. PRE-UPDATE SHOULD NOT_CHANGE_CREATED_AT
    // =========================================================

    @Test
    void onUpdate_shouldNotChangeCreatedAt()
            throws InterruptedException {

        Workflow workflow =
                new Workflow(
                        WORKFLOW_NAME,
                        DESCRIPTION,
                        WorkflowStatus.DRAFT,
                        CREATED_BY
                );

        LocalDateTime originalCreatedAt =
                workflow.getCreatedAt();

        Thread.sleep(2);

        workflow.onUpdate();

        assertEquals(
                originalCreatedAt,
                workflow.getCreatedAt()
        );
    }


    // =========================================================
    // 13. SET ALL MUTABLE FIELDS
    // =========================================================

    @Test
    void setters_shouldUpdateAllMutableFields() {

        Workflow workflow =
                new Workflow();

        workflow.setName(
                "Production Workflow"
        );

        workflow.setDescription(
                "Production workflow description"
        );

        workflow.setStatus(
                WorkflowStatus.COMPLETED
        );

        workflow.setCreatedBy(
                "owner@example.com"
        );

        assertEquals(
                "Production Workflow",
                workflow.getName()
        );

        assertEquals(
                "Production workflow description",
                workflow.getDescription()
        );

        assertEquals(
                WorkflowStatus.COMPLETED,
                workflow.getStatus()
        );

        assertEquals(
                "owner@example.com",
                workflow.getCreatedBy()
        );
    }
}
