
package com.novawavex.novawavex.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStatusTest {

    // =========================================================
    // 1. ENUM VALUES
    // =========================================================

    @Test
    void shouldContainAllExpectedStatuses() {

        ExecutionStatus[] statuses =
                ExecutionStatus.values();

        assertArrayEquals(
                new ExecutionStatus[]{
                        ExecutionStatus.QUEUED,
                        ExecutionStatus.RUNNING,
                        ExecutionStatus.COMPLETED,
                        ExecutionStatus.FAILED,
                        ExecutionStatus.CANCELLED
                },
                statuses
        );
    }


    // =========================================================
    // 2. QUEUED → RUNNING
    // =========================================================

    @Test
    void queued_shouldTransitionToRunning() {

        assertTrue(
                ExecutionStatus.QUEUED.canTransitionTo(
                        ExecutionStatus.RUNNING
                )
        );
    }


    // =========================================================
    // 3. QUEUED → CANCELLED
    // =========================================================

    @Test
    void queued_shouldTransitionToCancelled() {

        assertTrue(
                ExecutionStatus.QUEUED.canTransitionTo(
                        ExecutionStatus.CANCELLED
                )
        );
    }


    // =========================================================
    // 4. QUEUED → COMPLETED NOT ALLOWED
    // =========================================================

    @Test
    void queued_shouldNotTransitionToCompleted() {

        assertFalse(
                ExecutionStatus.QUEUED.canTransitionTo(
                        ExecutionStatus.COMPLETED
                )
        );
    }


    // =========================================================
    // 5. QUEUED → FAILED NOT ALLOWED
    // =========================================================

    @Test
    void queued_shouldNotTransitionToFailed() {

        assertFalse(
                ExecutionStatus.QUEUED.canTransitionTo(
                        ExecutionStatus.FAILED
                )
        );
    }


    // =========================================================
    // 6. RUNNING → COMPLETED
    // =========================================================

    @Test
    void running_shouldTransitionToCompleted() {

        assertTrue(
                ExecutionStatus.RUNNING.canTransitionTo(
                        ExecutionStatus.COMPLETED
                )
        );
    }


    // =========================================================
    // 7. RUNNING → FAILED
    // =========================================================

    @Test
    void running_shouldTransitionToFailed() {

        assertTrue(
                ExecutionStatus.RUNNING.canTransitionTo(
                        ExecutionStatus.FAILED
                )
        );
    }


    // =========================================================
    // 8. RUNNING → CANCELLED
    // =========================================================

    @Test
    void running_shouldTransitionToCancelled() {

        assertTrue(
                ExecutionStatus.RUNNING.canTransitionTo(
                        ExecutionStatus.CANCELLED
                )
        );
    }


    // =========================================================
    // 9. RUNNING → QUEUED NOT ALLOWED
    // =========================================================

    @Test
    void running_shouldNotTransitionToQueued() {

        assertFalse(
                ExecutionStatus.RUNNING.canTransitionTo(
                        ExecutionStatus.QUEUED
                )
        );
    }


    // =========================================================
    // 10. COMPLETED IS TERMINAL
    // =========================================================

    @Test
    void completed_shouldNotTransitionToAnyStatus() {

        for (ExecutionStatus status :
                ExecutionStatus.values()) {

            assertFalse(
                    ExecutionStatus.COMPLETED
                            .canTransitionTo(status)
            );
        }
    }


    // =========================================================
    // 11. FAILED IS TERMINAL
    // =========================================================

    @Test
    void failed_shouldNotTransitionToAnyStatus() {

        for (ExecutionStatus status :
                ExecutionStatus.values()) {

            assertFalse(
                    ExecutionStatus.FAILED
                            .canTransitionTo(status)
            );
        }
    }


    // =========================================================
    // 12. CANCELLED IS TERMINAL
    // =========================================================

    @Test
    void cancelled_shouldNotTransitionToAnyStatus() {

        for (ExecutionStatus status :
                ExecutionStatus.values()) {

            assertFalse(
                    ExecutionStatus.CANCELLED
                            .canTransitionTo(status)
            );
        }
    }


    // =========================================================
    // 13. NULL TRANSITION
    // =========================================================

    @Test
    void canTransitionTo_null_shouldReturnFalse() {

        for (ExecutionStatus status :
                ExecutionStatus.values()) {

            assertFalse(
                    status.canTransitionTo(null)
            );
        }
    }
}
