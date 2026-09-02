
package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowStepTest {

    private static final String WORKFLOW_NAME =
            "Test Workflow";

    private static final String WORKFLOW_DESCRIPTION =
            "Test workflow description";

    private static final String CREATED_BY =
            "userA@example.com";

    private static final String STEP_NAME =
            "Send Notification";

    private static final String STEP_DESCRIPTION =
            "Send a workflow notification";


    // =========================================================
    // 1. DEFAULT CONSTRUCTOR
    // =========================================================

    @Test
    void defaultConstructor_shouldCreateStep() {

        WorkflowStep step =
                new WorkflowStep();

        assertNotNull(step);

        assertNull(step.getId());
        assertNull(step.getWorkflow());
        assertNull(step.getName());
        assertNull(step.getDescription());
        assertNull(step.getStepOrder());
        assertNull(step.getCreatedAt());
    }


    // =========================================================
    // 2. PARAMETERIZED CONSTRUCTOR
    // =========================================================

    @Test
    void parameterizedConstructor_shouldInitializeStep() {

        Workflow workflow =
                createWorkflow();

        WorkflowStep step =
                new WorkflowStep(
                        workflow,
                        STEP_NAME,
                        STEP_DESCRIPTION,
                        1
                );

        assertNull(step.getId());

        assertEquals(
                workflow,
                step.getWorkflow()
        );

        assertEquals(
                STEP_NAME,
                step.getName()
        );

        assertEquals(
                STEP_DESCRIPTION,
                step.getDescription()
        );

        assertEquals(
                1,
                step.getStepOrder()
        );

        assertNotNull(
                step.getCreatedAt()
        );
    }


    // =========================================================
    // 3. CONSTRUCTOR TIMESTAMP
    // =========================================================

    @Test
    void parameterizedConstructor_shouldSetCreatedAt() {

        LocalDateTime before =
                LocalDateTime.now();

        WorkflowStep step =
                new WorkflowStep(
                        createWorkflow(),
                        STEP_NAME,
                        STEP_DESCRIPTION,
                        1
                );

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                step.getCreatedAt()
        );

        assertFalse(
                step.getCreatedAt()
                        .isBefore(before)
        );

        assertFalse(
                step.getCreatedAt()
                        .isAfter(after)
        );
    }


    // =========================================================
    // 4. SET WORKFLOW
    // =========================================================

    @Test
    void setWorkflow_shouldUpdateWorkflow() {

        WorkflowStep step =
                new WorkflowStep();

        Workflow workflow =
                createWorkflow();

        step.setWorkflow(
                workflow
        );

        assertEquals(
                workflow,
                step.getWorkflow()
        );
    }


    // =========================================================
    // 5. SET NAME
    // =========================================================

    @Test
    void setName_shouldUpdateName() {

        WorkflowStep step =
                new WorkflowStep();

        step.setName(
                "Updated Step"
        );

        assertEquals(
                "Updated Step",
                step.getName()
        );
    }


    // =========================================================
    // 6. SET DESCRIPTION
    // =========================================================

    @Test
    void setDescription_shouldUpdateDescription() {

        WorkflowStep step =
                new WorkflowStep();

        step.setDescription(
                "Updated step description"
        );

        assertEquals(
                "Updated step description",
                step.getDescription()
        );
    }


    // =========================================================
    // 7. SET STEP ORDER
    // =========================================================

    @Test
    void setStepOrder_shouldUpdateStepOrder() {

        WorkflowStep step =
                new WorkflowStep();

        step.setStepOrder(
                5
        );

        assertEquals(
                5,
                step.getStepOrder()
        );
    }


    // =========================================================
    // 8. PRE-PERSIST WITH NULL CREATED AT
    // =========================================================

    @Test
    void onCreate_shouldSetCreatedAtWhenNull() {

        WorkflowStep step =
                new WorkflowStep();

        LocalDateTime before =
                LocalDateTime.now();

        step.onCreate();

        LocalDateTime after =
                LocalDateTime.now();

        assertNotNull(
                step.getCreatedAt()
        );

        assertFalse(
                step.getCreatedAt()
                        .isBefore(before)
        );

        assertFalse(
                step.getCreatedAt()
                        .isAfter(after)
        );
    }


    // =========================================================
    // 9. PRE-PERSIST SHOULD PRESERVE EXISTING CREATED AT
    // =========================================================

    @Test
    void onCreate_shouldPreserveExistingCreatedAt() {

        WorkflowStep step =
                new WorkflowStep(
                        createWorkflow(),
                        STEP_NAME,
                        STEP_DESCRIPTION,
                        1
                );

        LocalDateTime originalCreatedAt =
                step.getCreatedAt();

        step.onCreate();

        assertEquals(
                originalCreatedAt,
                step.getCreatedAt()
        );
    }


    // =========================================================
    // 10. COMPLETE STEP DATA
    // =========================================================

    @Test
    void step_shouldStoreCompleteStepInformation() {

        Workflow workflow =
                createWorkflow();

        WorkflowStep step =
                new WorkflowStep(
                        workflow,
                        "Validate User",
                        "Validate the authenticated user",
                        2
                );

        assertEquals(
                workflow,
                step.getWorkflow()
        );

        assertEquals(
                "Validate User",
                step.getName()
        );

        assertEquals(
                "Validate the authenticated user",
                step.getDescription()
        );

        assertEquals(
                2,
                step.getStepOrder()
        );

        assertNotNull(
                step.getCreatedAt()
        );
    }


    // =========================================================
    // 11. STEP ORDER CAN BE UPDATED
    // =========================================================

    @Test
    void setStepOrder_shouldAllowChangingOrder() {

        WorkflowStep step =
                new WorkflowStep(
                        createWorkflow(),
                        STEP_NAME,
                        STEP_DESCRIPTION,
                        1
                );

        assertEquals(
                1,
                step.getStepOrder()
        );

        step.setStepOrder(
                10
        );

        assertEquals(
                10,
                step.getStepOrder()
        );
    }


    // =========================================================
    // 12. DESCRIPTION CAN BE NULL
    // =========================================================

    @Test
    void setDescription_shouldAllowNull() {

        WorkflowStep step =
                new WorkflowStep(
                        createWorkflow(),
                        STEP_NAME,
                        STEP_DESCRIPTION,
                        1
                );

        step.setDescription(
                null
        );

        assertNull(
                step.getDescription()
        );
    }


    // =========================================================
    // HELPER
    // =========================================================

    private Workflow createWorkflow() {

        return new Workflow(
                WORKFLOW_NAME,
                WORKFLOW_DESCRIPTION,
                WorkflowStatus.DRAFT,
                CREATED_BY
        );
    }
}
