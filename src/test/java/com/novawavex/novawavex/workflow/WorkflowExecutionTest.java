
package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowExecutionTest {

    private static final String WORKFLOW_NAME =
            "Test Workflow";

    private static final String DESCRIPTION =
            "Test workflow description";

    private static final String USER_A =
            "userA@example.com";


    // =========================================================
    // 1. DEFAULT CONSTRUCTOR
    // =========================================================

    @Test
    void defaultConstructor_shouldCreateExecution() {

        WorkflowExecution execution =
                new WorkflowExecution();

        assertNotNull(execution);

        assertNull(execution.getId());
        assertNull(execution.getWorkflow());
        assertNull(execution.getStatus());
        assertNull(execution.getStartedAt());
        assertNull(execution.getCompletedAt());
        assertNull(execution.getCreatedAt());
        assertNull(execution.getCreatedBy());
        assertNull(execution.getUpdatedAt());
        assertNull(execution.getUpdatedBy());
        assertNull(execution.getErrorMessage());
    }


    // =========================================================
    // 2. PARAMETERIZED CONSTRUCTOR
    // =========================================================

    @Test
    void parameterizedConstructor_shouldInitializeExecution() {

        Workflow workflow =
                createWorkflow();

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflow,
                        USER_A
                );

        assertNull(execution.getId());

        assertEquals(
                workflow,
                execution.getWorkflow()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                execution.getStatus()
        );

        assertEquals(
                USER_A,
                execution.getCreatedBy()
        );

        assertEquals(
                USER_A,
                execution.getUpdatedBy()
        );

        assertNotNull(
                execution.getCreatedAt()
        );

        assertNotNull(
                execution.getUpdatedAt()
        );

        assertNull(
                execution.getStartedAt()
        );

        assertNull(
                execution.getCompletedAt()
        );

        assertNull(
                execution.getErrorMessage()
        );
    }


    // =========================================================
    // 3. CONSTRUCTOR TIMESTAMPS
    // =========================================================

    @Test
    void parameterizedConstructor_shouldSetTimestamps() {

        LocalDateTime before =
                LocalDateTime.now();

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                execution.getCreatedAt()
        );

        assertNotNull(
                execution.getUpdatedAt()
        );

        assertFalse(
                execution.getCreatedAt()
                        .isBefore(before)
        );

        assertFalse(
                execution.getCreatedAt()
                        .isAfter(after)
        );

        assertFalse(
                execution.getUpdatedAt()
                        .isBefore(before)
        );

        assertFalse(
                execution.getUpdatedAt()
                        .isAfter(after)
        );
    }


    // =========================================================
    // 4. CREATED AND UPDATED BY
    // =========================================================

    @Test
    void parameterizedConstructor_shouldSetCreatedByAndUpdatedBy() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        assertEquals(
                USER_A,
                execution.getCreatedBy()
        );

        assertEquals(
                USER_A,
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 5. SET WORKFLOW
    // =========================================================

    @Test
    void setWorkflow_shouldUpdateWorkflow() {

        WorkflowExecution execution =
                new WorkflowExecution();

        Workflow workflow =
                createWorkflow();

        execution.setWorkflow(
                workflow
        );

        assertEquals(
                workflow,
                execution.getWorkflow()
        );
    }


    // =========================================================
    // 6. SET STATUS
    // =========================================================

    @Test
    void setStatus_shouldUpdateStatus() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        assertEquals(
                ExecutionStatus.RUNNING,
                execution.getStatus()
        );
    }


    // =========================================================
    // 7. SET STARTED AT
    // =========================================================

    @Test
    void setStartedAt_shouldUpdateStartedAt() {

        WorkflowExecution execution =
                new WorkflowExecution();

        LocalDateTime startedAt =
                LocalDateTime.now();

        execution.setStartedAt(
                startedAt
        );

        assertEquals(
                startedAt,
                execution.getStartedAt()
        );
    }


    // =========================================================
    // 8. SET COMPLETED AT
    // =========================================================

    @Test
    void setCompletedAt_shouldUpdateCompletedAt() {

        WorkflowExecution execution =
                new WorkflowExecution();

        LocalDateTime completedAt =
                LocalDateTime.now();

        execution.setCompletedAt(
                completedAt
        );

        assertEquals(
                completedAt,
                execution.getCompletedAt()
        );
    }


    // =========================================================
    // 9. SET CREATED BY
    // =========================================================

    @Test
    void setCreatedBy_shouldUpdateCreatedBy() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setCreatedBy(
                "newuser@example.com"
        );

        assertEquals(
                "newuser@example.com",
                execution.getCreatedBy()
        );
    }


    // =========================================================
    // 10. SET UPDATED BY
    // =========================================================

    @Test
    void setUpdatedBy_shouldUpdateUpdatedBy() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setUpdatedBy(
                "updated@example.com"
        );

        assertEquals(
                "updated@example.com",
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 11. SET ERROR MESSAGE
    // =========================================================

    @Test
    void setErrorMessage_shouldUpdateErrorMessage() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setErrorMessage(
                "Execution failed"
        );

        assertEquals(
                "Execution failed",
                execution.getErrorMessage()
        );
    }


    // =========================================================
    // 12. PRE-PERSIST WITH NULL VALUES
    // =========================================================

    @Test
    void onCreate_shouldSetMissingValues() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setCreatedBy(
                USER_A
        );

        LocalDateTime before =
                LocalDateTime.now();

        execution.onCreate();

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                execution.getCreatedAt()
        );

        assertNotNull(
                execution.getUpdatedAt()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                execution.getStatus()
        );

        assertEquals(
                USER_A,
                execution.getUpdatedBy()
        );

        assertFalse(
                execution.getCreatedAt()
                        .isBefore(before)
        );

        assertFalse(
                execution.getCreatedAt()
                        .isAfter(after)
        );

        assertFalse(
                execution.getUpdatedAt()
                        .isBefore(before)
        );

        assertFalse(
                execution.getUpdatedAt()
                        .isAfter(after)
        );
    }


    // =========================================================
    // 13. PRE-PERSIST SHOULD PRESERVE EXISTING VALUES
    // =========================================================

    @Test
    void onCreate_shouldPreserveExistingValues() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        LocalDateTime originalCreatedAt =
                execution.getCreatedAt();

        LocalDateTime originalUpdatedAt =
                execution.getUpdatedAt();

        ExecutionStatus originalStatus =
                execution.getStatus();

        String originalUpdatedBy =
                execution.getUpdatedBy();

        execution.onCreate();

        assertEquals(
                originalCreatedAt,
                execution.getCreatedAt()
        );

        assertEquals(
                originalUpdatedAt,
                execution.getUpdatedAt()
        );

        assertEquals(
                originalStatus,
                execution.getStatus()
        );

        assertEquals(
                originalUpdatedBy,
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 14. PRE-UPDATE
    // =========================================================

    @Test
    void onUpdate_shouldUpdateUpdatedAt()
            throws InterruptedException {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        LocalDateTime originalUpdatedAt =
                execution.getUpdatedAt();

        Thread.sleep(2);

        execution.onUpdate();

        assertNotNull(
                execution.getUpdatedAt()
        );

        assertTrue(
                execution.getUpdatedAt()
                        .isAfter(originalUpdatedAt)
        );
    }


    // =========================================================
    // 15. PRE-UPDATE SHOULD PRESERVE UPDATED BY
    // =========================================================

    @Test
    void onUpdate_shouldPreserveExistingUpdatedBy()
            throws InterruptedException {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        execution.setUpdatedBy(
                "updated@example.com"
        );

        Thread.sleep(2);

        execution.onUpdate();

        assertEquals(
                "updated@example.com",
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 16. PRE-UPDATE SHOULD FALL BACK TO CREATED BY
    // =========================================================

    @Test
    void onUpdate_shouldSetUpdatedByFromCreatedByWhenNull() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setCreatedBy(
                USER_A
        );

        execution.setUpdatedBy(
                null
        );

        execution.onUpdate();

        assertEquals(
                USER_A,
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 17. PRE-PERSIST SHOULD_SET_UPDATED_BY_FROM_CREATED_BY
    // =========================================================

    @Test
    void onCreate_shouldSetUpdatedByFromCreatedByWhenNull() {

        WorkflowExecution execution =
                new WorkflowExecution();

        execution.setCreatedBy(
                USER_A
        );

        execution.setUpdatedBy(
                null
        );

        execution.onCreate();

        assertEquals(
                USER_A,
                execution.getUpdatedBy()
        );
    }


    // =========================================================
    // 18. EXECUTION LIFECYCLE FIELDS
    // =========================================================

    @Test
    void executionLifecycleFields_shouldBeSetCorrectly() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        LocalDateTime startedAt =
                LocalDateTime.now();

        LocalDateTime completedAt =
                startedAt.plusSeconds(5);

        execution.setStatus(
                ExecutionStatus.COMPLETED
        );

        execution.setStartedAt(
                startedAt
        );

        execution.setCompletedAt(
                completedAt
        );

        assertEquals(
                ExecutionStatus.COMPLETED,
                execution.getStatus()
        );

        assertEquals(
                startedAt,
                execution.getStartedAt()
        );

        assertEquals(
                completedAt,
                execution.getCompletedAt()
        );
    }


    // =========================================================
    // 19. FAILED EXECUTION FIELDS
    // =========================================================

    @Test
    void failedExecution_shouldStoreErrorInformation() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        createWorkflow(),
                        USER_A
                );

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        execution.setErrorMessage(
                "Connection timeout"
        );

        assertEquals(
                ExecutionStatus.FAILED,
                execution.getStatus()
        );

        assertEquals(
                "Connection timeout",
                execution.getErrorMessage()
        );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private Workflow createWorkflow() {

        return new Workflow(
                WORKFLOW_NAME,
                DESCRIPTION,
                WorkflowStatus.DRAFT,
                USER_A
        );
    }
}
