package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowStatusTest {

    // =========================================================
    // 1. EXPECTED WORKFLOW STATUSES
    // =========================================================

    @Test
    void shouldContainAllExpectedStatuses() {

        assertNotNull(WorkflowStatus.DRAFT);
        assertNotNull(WorkflowStatus.ACTIVE);
        assertNotNull(WorkflowStatus.COMPLETED);
        assertNotNull(WorkflowStatus.CANCELLED);
    }


    // =========================================================
    // 2. STATUS COUNT
    // =========================================================

    @Test
    void shouldContainExactlyFourStatuses() {

        assertEquals(
                4,
                WorkflowStatus.values().length
        );
    }


    // =========================================================
    // 3. STATUS NAMES
    // =========================================================

    @Test
    void shouldHaveExpectedStatusNames() {

        assertArrayEquals(
                new String[]{
                        "DRAFT",
                        "ACTIVE",
                        "COMPLETED",
                        "CANCELLED"
                },
                new String[]{
                        WorkflowStatus.DRAFT.name(),
                        WorkflowStatus.ACTIVE.name(),
                        WorkflowStatus.COMPLETED.name(),
                        WorkflowStatus.CANCELLED.name()
                }
        );
    }


    // =========================================================
    // 4. VALUE OF
    // =========================================================

    @Test
    void valueOf_shouldReturnCorrectStatus() {

        assertEquals(
                WorkflowStatus.DRAFT,
                WorkflowStatus.valueOf("DRAFT")
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                WorkflowStatus.valueOf("ACTIVE")
        );

        assertEquals(
                WorkflowStatus.COMPLETED,
                WorkflowStatus.valueOf("COMPLETED")
        );

        assertEquals(
                WorkflowStatus.CANCELLED,
                WorkflowStatus.valueOf("CANCELLED")
        );
    }


    // =========================================================
    // 5. INVALID VALUE
    // =========================================================

    @Test
    void valueOf_withInvalidStatus_shouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> WorkflowStatus.valueOf("INVALID")
        );
    }


    // =========================================================
    // 6. NULL VALUE
    // =========================================================

    @Test
    void valueOf_withNull_shouldThrowException() {

        assertThrows(
                NullPointerException.class,
                () -> WorkflowStatus.valueOf(null)
        );
    }
}