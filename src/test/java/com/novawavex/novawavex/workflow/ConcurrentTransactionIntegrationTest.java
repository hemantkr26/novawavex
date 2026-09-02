
package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConcurrentTransactionIntegrationTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowExecutionRepository executionRepository;

    @Autowired
    private WorkflowExecutionService executionService;

    @Autowired
    private UserRepository userRepository;


    /*
     * =========================================
     * TEST USER
     * =========================================
     */

    private User testUser;


    /*
     * =========================================
     * SETUP
     * =========================================
     */

    @BeforeEach
    void setUp() {

        testUser =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test user not found"
                                )
                        );
    }


    /*
     * =========================================
     * T15.10.1
     *
     * CREATE MULTIPLE EXECUTIONS CONCURRENTLY
     * =========================================
     */

    @Test
    void concurrentExecutionCreation_shouldRemainConsistent()
            throws Exception {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        int threadCount = 5;

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        threadCount
                );

        CountDownLatch readyLatch =
                new CountDownLatch(
                        threadCount
                );

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch finishLatch =
                new CountDownLatch(
                        threadCount
                );

        try {

            for (int i = 0; i < threadCount; i++) {

                executor.submit(() -> {

                    readyLatch.countDown();

                    try {

                        startLatch.await();

                        executionService.createExecution(
                                workflow,
                                testUser.getEmail()
                        );

                    } catch (Exception exception) {

                        fail(
                                "Concurrent execution creation failed: "
                                        + exception.getMessage()
                        );

                    } finally {

                        finishLatch.countDown();
                    }
                });
            }

            assertTrue(
                    readyLatch.await(
                            10,
                            TimeUnit.SECONDS
                    ),
                    "Worker threads were not ready"
            );

            startLatch.countDown();

            assertTrue(
                    finishLatch.await(
                            30,
                            TimeUnit.SECONDS
                    ),
                    "Concurrent execution creation timed out"
            );

        } finally {

            executor.shutdown();

            assertTrue(
                    executor.awaitTermination(
                            30,
                            TimeUnit.SECONDS
                    ),
                    "Executor did not terminate"
            );
        }

        List<WorkflowExecution> executions =
                executionRepository
                        .findByWorkflowIdAndCreatedBy(
                                workflow.getId(),
                                testUser.getEmail()
                        );

        assertTrue(
                executions.size() >= threadCount,
                "Expected at least "
                        + threadCount
                        + " executions"
        );

        long queuedCount =
                executions.stream()
                        .filter(execution ->
                                execution.getStatus()
                                        == ExecutionStatus.QUEUED
                        )
                        .count();

        assertTrue(
                queuedCount >= threadCount,
                "Concurrent executions were not persisted as QUEUED"
        );
    }


    /*
     * =========================================
     * T15.10.2
     *
     * EXECUTION CREATION SHOULD SET
     * REQUIRED AUDIT INFORMATION
     * =========================================
     */

    @Test
    void createExecution_shouldMaintainAuditInformation() {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        WorkflowExecution execution =
                executionService.createExecution(
                        workflow,
                        testUser.getEmail()
                );

        assertNotNull(
                execution.getId()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                execution.getStatus()
        );

        assertEquals(
                testUser.getEmail(),
                execution.getCreatedBy()
        );

        assertEquals(
                testUser.getEmail(),
                execution.getUpdatedBy()
        );

        assertNotNull(
                execution.getCreatedAt()
        );

        assertNotNull(
                execution.getUpdatedAt()
        );
    }


    /*
     * =========================================
     * T15.10.3
     *
     * TRANSACTIONAL EXECUTION TRANSITION
     * =========================================
     */

    @Test
    void executionTransition_shouldPersistStatusAndTimestamps() {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        WorkflowExecution execution =
                executionService.createExecution(
                        workflow,
                        testUser.getEmail()
                );

        Long executionId =
                execution.getId();

        WorkflowExecution started =
                executionService.startExecution(
                        executionId,
                        testUser.getEmail()
                );

        assertEquals(
                ExecutionStatus.RUNNING,
                started.getStatus()
        );

        assertNotNull(
                started.getStartedAt()
        );

        WorkflowExecution completed =
                executionService.completeExecution(
                        executionId,
                        testUser.getEmail()
                );

        assertEquals(
                ExecutionStatus.COMPLETED,
                completed.getStatus()
        );

        assertNotNull(
                completed.getStartedAt()
        );

        assertNotNull(
                completed.getCompletedAt()
        );

        WorkflowExecution persisted =
                executionRepository
                        .findById(
                                executionId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Execution was not persisted"
                                )
                        );

        assertEquals(
                ExecutionStatus.COMPLETED,
                persisted.getStatus()
        );

        assertNotNull(
                persisted.getStartedAt()
        );

        assertNotNull(
                persisted.getCompletedAt()
        );
    }


    /*
     * =========================================
     * T15.10.4
     *
     * FAILED EXECUTION SHOULD PRESERVE
     * ERROR INFORMATION
     * =========================================
     */

    @Test
    void failedExecution_shouldPersistErrorInformation() {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        WorkflowExecution execution =
                executionService.createExecution(
                        workflow,
                        testUser.getEmail()
                );

        executionService.startExecution(
                execution.getId(),
                testUser.getEmail()
        );

        String errorMessage =
                "T15.10 transaction integrity test failure";

        WorkflowExecution failed =
                executionService.failExecution(
                        execution.getId(),
                        errorMessage,
                        testUser.getEmail()
                );

        assertEquals(
                ExecutionStatus.FAILED,
                failed.getStatus()
        );

        assertEquals(
                errorMessage,
                failed.getErrorMessage()
        );

        assertNotNull(
                failed.getCompletedAt()
        );

        WorkflowExecution persisted =
                executionRepository
                        .findById(
                                execution.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Failed execution was not persisted"
                                )
                        );

        assertEquals(
                ExecutionStatus.FAILED,
                persisted.getStatus()
        );

        assertEquals(
                errorMessage,
                persisted.getErrorMessage()
        );
    }


    /*
     * =========================================
     * T15.10.5
     *
     * RETRY SHOULD CREATE A NEW EXECUTION
     * WITHOUT MODIFYING THE FAILED ONE
     * =========================================
     */

    @Test
    void retryExecution_shouldCreateIndependentExecution() {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        WorkflowExecution original =
                executionService.createExecution(
                        workflow,
                        testUser.getEmail()
                );

        Long originalExecutionId =
                original.getId();

        executionService.startExecution(
                originalExecutionId,
                testUser.getEmail()
        );

        executionService.failExecution(
                originalExecutionId,
                "T15.10 retry test failure",
                testUser.getEmail()
        );

        /*
         * =====================================
         * RELOAD ORIGINAL EXECUTION
         *
         * The service transaction may leave the
         * original Java object with an older state.
         *
         * The repository reload verifies the
         * actual persisted database state.
         * =====================================
         */

        WorkflowExecution failedOriginal =
                executionRepository
                        .findById(
                                originalExecutionId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Original execution was not persisted"
                                )
                        );

        assertEquals(
                ExecutionStatus.FAILED,
                failedOriginal.getStatus()
        );

        assertEquals(
                "T15.10 retry test failure",
                failedOriginal.getErrorMessage()
        );

        /*
         * =====================================
         * RETRY
         * =====================================
         */

        WorkflowExecution retry =
                executionService.retryExecution(
                        originalExecutionId,
                        testUser.getEmail()
                );

        assertNotNull(
                retry.getId()
        );

        assertNotEquals(
                originalExecutionId,
                retry.getId()
        );

        /*
         * =====================================
         * VERIFY ORIGINAL REMAINS FAILED
         * =====================================
         */

        WorkflowExecution persistedOriginal =
                executionRepository
                        .findById(
                                originalExecutionId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Original execution disappeared"
                                )
                        );

        assertEquals(
                ExecutionStatus.FAILED,
                persistedOriginal.getStatus()
        );

        /*
         * =====================================
         * VERIFY RETRY IS NEW AND QUEUED
         * =====================================
         */

        assertEquals(
                ExecutionStatus.QUEUED,
                retry.getStatus()
        );

        assertEquals(
                workflow.getId(),
                retry.getWorkflow().getId()
        );

        assertEquals(
                testUser.getEmail(),
                retry.getCreatedBy()
        );

        WorkflowExecution persistedRetry =
                executionRepository
                        .findById(
                                retry.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Retry execution was not persisted"
                                )
                        );

        assertEquals(
                ExecutionStatus.QUEUED,
                persistedRetry.getStatus()
        );

        assertNotEquals(
                persistedOriginal.getId(),
                persistedRetry.getId()
        );
    }


    /*
     * =========================================
     * T15.10.6
     *
     * USER EXECUTION HISTORY SHOULD REMAIN
     * CONSISTENT AFTER MULTIPLE OPERATIONS
     * =========================================
     */

    @Test
    void userExecutionHistory_shouldRemainConsistent() {

        Workflow workflow =
                workflowRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Test workflow not found"
                                )
                        );

        int beforeCount =
                executionRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        )
                        .size();

        WorkflowExecution execution =
                executionService.createExecution(
                        workflow,
                        testUser.getEmail()
                );

        executionService.startExecution(
                execution.getId(),
                testUser.getEmail()
        );

        executionService.completeExecution(
                execution.getId(),
                testUser.getEmail()
        );

        List<WorkflowExecution> history =
                executionRepository
                        .findByCreatedBy(
                                testUser.getEmail()
                        );

        assertEquals(
                beforeCount + 1,
                history.size()
        );

        WorkflowExecution persisted =
                history.stream()
                        .filter(item ->
                                item.getId()
                                        .equals(
                                                execution.getId()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new AssertionError(
                                        "Execution missing from user history"
                                )
                        );

        assertEquals(
                ExecutionStatus.COMPLETED,
                persisted.getStatus()
        );

        assertNotNull(
                persisted.getStartedAt()
        );

        assertNotNull(
                persisted.getCompletedAt()
        );
    }
}
