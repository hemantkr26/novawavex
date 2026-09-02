
package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowStepRelationTest {

    private Workflow workflow;

    private WorkflowStep sourceStep;

    private WorkflowStep targetStep;

    private WorkflowStepRelation relation;


    @BeforeEach
    void setUp() {

        workflow = new Workflow(
                "Test Workflow",
                "Test workflow description",
                WorkflowStatus.DRAFT,
                "userA@example.com"
        );

        sourceStep = new WorkflowStep(
                workflow,
                "Source Step",
                "Source step description",
                1
        );

        targetStep = new WorkflowStep(
                workflow,
                "Target Step",
                "Target step description",
                2
        );

        relation = new WorkflowStepRelation(
                workflow,
                sourceStep,
                targetStep
        );
    }


    // =========================================================
    // 1. PARAMETERIZED CONSTRUCTOR
    // =========================================================

    @Test
    void parameterizedConstructor_shouldInitializeFields() {

        assertEquals(
                workflow,
                relation.getWorkflow()
        );

        assertEquals(
                sourceStep,
                relation.getSourceStep()
        );

        assertEquals(
                targetStep,
                relation.getTargetStep()
        );

        assertNotNull(
                relation.getCreatedAt()
        );
    }


    // =========================================================
    // 2. DEFAULT CONSTRUCTOR
    // =========================================================

    @Test
    void defaultConstructor_shouldCreateEmptyRelation() {

        WorkflowStepRelation emptyRelation =
                new WorkflowStepRelation();

        assertNull(
                emptyRelation.getId()
        );

        assertNull(
                emptyRelation.getWorkflow()
        );

        assertNull(
                emptyRelation.getSourceStep()
        );

        assertNull(
                emptyRelation.getTargetStep()
        );

        assertNull(
                emptyRelation.getCreatedAt()
        );
    }


    // =========================================================
    // 3. WORKFLOW GETTER
    // =========================================================

    @Test
    void getWorkflow_shouldReturnWorkflow() {

        assertSame(
                workflow,
                relation.getWorkflow()
        );
    }


    // =========================================================
    // 4. SOURCE STEP GETTER
    // =========================================================

    @Test
    void getSourceStep_shouldReturnSourceStep() {

        assertSame(
                sourceStep,
                relation.getSourceStep()
        );
    }


    // =========================================================
    // 5. TARGET STEP GETTER
    // =========================================================

    @Test
    void getTargetStep_shouldReturnTargetStep() {

        assertSame(
                targetStep,
                relation.getTargetStep()
        );
    }


    // =========================================================
    // 6. CREATED AT
    // =========================================================

    @Test
    void parameterizedConstructor_shouldSetCreatedAt() {

        LocalDateTime createdAt =
                relation.getCreatedAt();

        assertNotNull(createdAt);

        assertTrue(
                !createdAt.isAfter(
                        LocalDateTime.now()
                )
        );
    }


    // =========================================================
    // 7. SET WORKFLOW
    // =========================================================

    @Test
    void setWorkflow_shouldUpdateWorkflow() {

        Workflow newWorkflow = new Workflow(
                "New Workflow",
                "New description",
                WorkflowStatus.ACTIVE,
                "userB@example.com"
        );

        relation.setWorkflow(newWorkflow);

        assertSame(
                newWorkflow,
                relation.getWorkflow()
        );
    }


    // =========================================================
    // 8. SET SOURCE STEP
    // =========================================================

    @Test
    void setSourceStep_shouldUpdateSourceStep() {

        WorkflowStep newSourceStep =
                new WorkflowStep(
                        workflow,
                        "New Source",
                        "New source description",
                        3
                );

        relation.setSourceStep(
                newSourceStep
        );

        assertSame(
                newSourceStep,
                relation.getSourceStep()
        );
    }


    // =========================================================
    // 9. SET TARGET STEP
    // =========================================================

    @Test
    void setTargetStep_shouldUpdateTargetStep() {

        WorkflowStep newTargetStep =
                new WorkflowStep(
                        workflow,
                        "New Target",
                        "New target description",
                        4
                );

        relation.setTargetStep(
                newTargetStep
        );

        assertSame(
                newTargetStep,
                relation.getTargetStep()
        );
    }


    // =========================================================
    // 10. PRE-PERSIST
    // =========================================================

    @Test
    void onCreate_whenCreatedAtIsNull_shouldSetTimestamp() {

        WorkflowStepRelation emptyRelation =
                new WorkflowStepRelation();

        assertNull(
                emptyRelation.getCreatedAt()
        );

        emptyRelation.onCreate();

        assertNotNull(
                emptyRelation.getCreatedAt()
        );
    }


    // =========================================================
    // 11. PRE-PERSIST PRESERVES EXISTING TIMESTAMP
    // =========================================================

    @Test
    void onCreate_whenCreatedAtAlreadyExists_shouldPreserveTimestamp() {

        LocalDateTime originalTimestamp =
                LocalDateTime.of(
                        2026,
                        1,
                        1,
                        10,
                        30
                );

        WorkflowStepRelation emptyRelation =
                new WorkflowStepRelation();

        /*
         * createdAt has no setter by design.
         * Use the entity constructor first so the
         * lifecycle behavior can be verified safely.
         */
        WorkflowStepRelation existingRelation =
                new WorkflowStepRelation(
                        workflow,
                        sourceStep,
                        targetStep
                );

        LocalDateTime constructorTimestamp =
                existingRelation.getCreatedAt();

        existingRelation.onCreate();

        assertEquals(
                constructorTimestamp,
                existingRelation.getCreatedAt()
        );

        assertNotNull(
                originalTimestamp
        );

        assertNull(
                emptyRelation.getCreatedAt()
        );
    }


    // =========================================================
    // 12. SOURCE AND TARGET CAN BE DIFFERENT
    // =========================================================

    @Test
    void relation_shouldMaintainSourceAndTargetSeparately() {

        assertNotSame(
                relation.getSourceStep(),
                relation.getTargetStep()
        );

        assertEquals(
                "Source Step",
                relation.getSourceStep().getName()
        );

        assertEquals(
                "Target Step",
                relation.getTargetStep().getName()
        );
    }
}
