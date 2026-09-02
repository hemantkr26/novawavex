package com.novawavex.novawavex.workflow;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class WorkflowExecutionRepositoryTest {

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    private Workflow workflowA;

    private Workflow workflowB;

    private final String USER_A =
            "execution-user-a@novawavex.com";

    private final String USER_B =
            "execution-user-b@novawavex.com";


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        /*
         * Do not delete existing workflows here.
         *
         * The application uses PostgreSQL and the workflows table
         * may already contain notifications referencing workflows.
         *
         * @DataJpaTest provides transactional test isolation and
         * rolls back test changes after each test.
         *
         * Therefore destructive cleanup is unnecessary and can
         * cause foreign-key violations.
         */

        workflowA =
                new Workflow(
                        "Execution Test Workflow A",
                        "Workflow used for execution repository tests",
                        WorkflowStatus.ACTIVE,
                        USER_A
                );

        workflowB =
                new Workflow(
                        "Execution Test Workflow B",
                        "Second workflow used for repository tests",
                        WorkflowStatus.DRAFT,
                        USER_B
                );

        workflowA =
                workflowRepository.save(
                        workflowA
                );

        workflowB =
                workflowRepository.save(
                        workflowB
                );
    }


    // =========================================================
    // 1. SAVE EXECUTION
    // =========================================================

    @Test
    void saveExecution_shouldPersistExecution() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution savedExecution =
                workflowExecutionRepository.save(
                        execution
                );

        assertNotNull(
                savedExecution.getId()
        );

        assertNotNull(
                savedExecution.getWorkflow()
        );

        assertEquals(
                workflowA.getId(),
                savedExecution.getWorkflow().getId()
        );

        assertEquals(
                ExecutionStatus.QUEUED,
                savedExecution.getStatus()
        );

        assertEquals(
                USER_A,
                savedExecution.getCreatedBy()
        );

        assertEquals(
                USER_A,
                savedExecution.getUpdatedBy()
        );

        assertNotNull(
                savedExecution.getCreatedAt()
        );

        assertNotNull(
                savedExecution.getUpdatedAt()
        );
    }


    // =========================================================
    // 2. FIND BY WORKFLOW ID
    // =========================================================

    @Test
    void findByWorkflowId_whenExecutionsExist_shouldReturnExecutions() {

        WorkflowExecution execution1 =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution execution2 =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution execution3 =
                new WorkflowExecution(
                        workflowB,
                        USER_B
                );

        workflowExecutionRepository.save(
                execution1
        );

        workflowExecutionRepository.save(
                execution2
        );

        workflowExecutionRepository.save(
                execution3
        );

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByWorkflowId(
                                workflowA.getId()
                        );

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(execution ->
                                execution.getWorkflow()
                                        .getId()
                                        .equals(
                                                workflowA.getId()
                                        )
                        )
        );
    }


    // =========================================================
    // 3. FIND BY WORKFLOW ID - NOT FOUND
    // =========================================================

    @Test
    void findByWorkflowId_whenNoExecutionsExist_shouldReturnEmptyList() {

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByWorkflowId(
                                workflowA.getId()
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 4. FIND BY CREATED BY
    // =========================================================

    @Test
    void findByCreatedBy_whenExecutionsExist_shouldReturnExecutions() {

        WorkflowExecution execution1 =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution execution2 =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution execution3 =
                new WorkflowExecution(
                        workflowB,
                        USER_B
                );

        workflowExecutionRepository.save(
                execution1
        );

        workflowExecutionRepository.save(
                execution2
        );

        workflowExecutionRepository.save(
                execution3
        );

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByCreatedBy(
                                USER_A
                        );

        assertEquals(
                2,
                result.size()
        );

        assertTrue(
                result.stream()
                        .allMatch(execution ->
                                USER_A.equals(
                                        execution.getCreatedBy()
                                )
                        )
        );
    }


    // =========================================================
    // 5. FIND BY CREATED BY - NOT FOUND
    // =========================================================

    @Test
    void findByCreatedBy_whenUserHasNoExecutions_shouldReturnEmptyList() {

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByCreatedBy(
                                "execution-missing@novawavex.com"
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 6. FIND BY WORKFLOW ID AND CREATED BY
    // =========================================================

    @Test
    void findByWorkflowIdAndCreatedBy_shouldReturnMatchingExecutions() {

        WorkflowExecution userAWorkflowA =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution userBWorkflowB =
                new WorkflowExecution(
                        workflowB,
                        USER_B
                );

        workflowExecutionRepository.save(
                userAWorkflowA
        );

        workflowExecutionRepository.save(
                userBWorkflowB
        );

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByWorkflowIdAndCreatedBy(
                                workflowA.getId(),
                                USER_A
                        );

        assertEquals(
                1,
                result.size()
        );

        WorkflowExecution found =
                result.get(0);

        assertEquals(
                workflowA.getId(),
                found.getWorkflow().getId()
        );

        assertEquals(
                USER_A,
                found.getCreatedBy()
        );
    }


    // =========================================================
    // 7. WORKFLOW ID MATCH BUT USER DOES NOT MATCH
    // =========================================================

    @Test
    void findByWorkflowIdAndCreatedBy_whenUserDoesNotMatch_shouldReturnEmptyList() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        workflowExecutionRepository.save(
                execution
        );

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByWorkflowIdAndCreatedBy(
                                workflowA.getId(),
                                USER_B
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 8. WORKFLOW ID DOES NOT MATCH
    // =========================================================

    @Test
    void findByWorkflowIdAndCreatedBy_whenWorkflowDoesNotMatch_shouldReturnEmptyList() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        workflowExecutionRepository.save(
                execution
        );

        List<WorkflowExecution> result =
                workflowExecutionRepository
                        .findByWorkflowIdAndCreatedBy(
                                workflowB.getId(),
                                USER_A
                        );

        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 9. EXECUTION STATUS PERSISTENCE
    // =========================================================

    @Test
    void saveExecution_shouldPersistExecutionStatus() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        execution.setStatus(
                ExecutionStatus.RUNNING
        );

        WorkflowExecution savedExecution =
                workflowExecutionRepository.save(
                        execution
                );

        Optional<WorkflowExecution> result =
                workflowExecutionRepository.findById(
                        savedExecution.getId()
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                ExecutionStatus.RUNNING,
                result.get().getStatus()
        );
    }


    // =========================================================
    // 10. EXECUTION TIMESTAMPS
    // =========================================================

    @Test
    void saveExecution_shouldPersistExecutionTimestamps() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        LocalDateTime startedAt =
                LocalDateTime.now()
                        .minusMinutes(5);

        LocalDateTime completedAt =
                LocalDateTime.now();

        execution.setStartedAt(
                startedAt
        );

        execution.setCompletedAt(
                completedAt
        );

        WorkflowExecution savedExecution =
                workflowExecutionRepository.save(
                        execution
                );

        Optional<WorkflowExecution> result =
                workflowExecutionRepository.findById(
                        savedExecution.getId()
                );

        assertTrue(
                result.isPresent()
        );

        assertNotNull(
                result.get().getStartedAt()
        );

        assertNotNull(
                result.get().getCompletedAt()
        );

        assertEquals(
                startedAt,
                result.get().getStartedAt()
        );

        assertEquals(
                completedAt,
                result.get().getCompletedAt()
        );
    }


    // =========================================================
    // 11. ERROR MESSAGE PERSISTENCE
    // =========================================================

    @Test
    void saveExecution_shouldPersistErrorMessage() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        execution.setStatus(
                ExecutionStatus.FAILED
        );

        execution.setErrorMessage(
                "Test execution failure"
        );

        WorkflowExecution savedExecution =
                workflowExecutionRepository.save(
                        execution
                );

        Optional<WorkflowExecution> result =
                workflowExecutionRepository.findById(
                        savedExecution.getId()
                );

        assertTrue(
                result.isPresent()
        );

        assertEquals(
                ExecutionStatus.FAILED,
                result.get().getStatus()
        );

        assertEquals(
                "Test execution failure",
                result.get().getErrorMessage()
        );
    }


    // =========================================================
    // 12. DELETE EXECUTION
    // =========================================================

    @Test
    void deleteExecution_shouldRemoveExecution() {

        WorkflowExecution execution =
                new WorkflowExecution(
                        workflowA,
                        USER_A
                );

        WorkflowExecution savedExecution =
                workflowExecutionRepository.save(
                        execution
                );

        Long executionId =
                savedExecution.getId();

        workflowExecutionRepository.delete(
                savedExecution
        );

        Optional<WorkflowExecution> result =
                workflowExecutionRepository.findById(
                        executionId
                );

        assertTrue(
                result.isEmpty()
        );
    }
}