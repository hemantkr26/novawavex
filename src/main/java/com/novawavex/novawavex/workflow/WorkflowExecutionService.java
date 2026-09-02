package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationService;
import com.novawavex.novawavex.notification.NotificationType;
import com.novawavex.novawavex.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WorkflowExecutionService {


private final WorkflowExecutionRepository executionRepository;

private final NotificationService notificationService;

private final UserRepository userRepository;

public WorkflowExecutionService(
        WorkflowExecutionRepository executionRepository,
        NotificationService notificationService,
        UserRepository userRepository) {

    this.executionRepository = executionRepository;
    this.notificationService = notificationService;
    this.userRepository = userRepository;
}

/*
 * =========================================
 * CREATE EXECUTION
 * =========================================
 */

public WorkflowExecution createExecution(
        Workflow workflow,
        String createdBy) {

    if (workflow == null) {
        throw new IllegalArgumentException(
                "Workflow cannot be null"
        );
    }

    if (createdBy == null || createdBy.isBlank()) {
        throw new IllegalArgumentException(
                "Created by cannot be empty"
        );
    }

    WorkflowExecution execution =
            new WorkflowExecution(
                    workflow,
                    createdBy
            );

    execution.setStatus(
            ExecutionStatus.QUEUED
    );

    return executionRepository.save(
            execution
    );
}

/*
 * =========================================
 * GET EXECUTION BY ID
 * =========================================
 *
 * Ownership-aware lookup.
 *
 * The execution must belong to the
 * authenticated user.
 */

@Transactional(readOnly = true)
public WorkflowExecution getExecutionById(
        Long executionId,
        String createdBy) {

    if (executionId == null) {
        throw new IllegalArgumentException(
                "Execution ID cannot be null"
        );
    }

    if (createdBy == null || createdBy.isBlank()) {
        throw new IllegalArgumentException(
                "Created by cannot be empty"
        );
    }

    return executionRepository
            .findByIdAndCreatedBy(
                    executionId,
                    createdBy
            )
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Execution not found"
                    )
            );
}

/*
 * =========================================
 * GET EXECUTION BY ID - INTERNAL LOOKUP
 * =========================================
 *
 * This method is intentionally kept for
 * internal database operations such as
 * workflow deletion.
 *
 * It should NOT be used by authenticated
 * user-facing execution endpoints when
 * ownership must be enforced.
 */

@Transactional(readOnly = true)
public WorkflowExecution getExecutionById(
        Long executionId) {

    if (executionId == null) {
        throw new IllegalArgumentException(
                "Execution ID cannot be null"
        );
    }

    return executionRepository
            .findById(executionId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Execution not found"
                    )
            );
}

/*
 * =========================================
 * GET WORKFLOW EXECUTIONS
 * =========================================
 */

@Transactional(readOnly = true)
public List<WorkflowExecution> getExecutionsByWorkflow(
        Long workflowId) {

    return executionRepository
            .findByWorkflowId(
                    workflowId
            );
}

/*
 * =========================================
 * GET USER EXECUTIONS
 * =========================================
 */

@Transactional(readOnly = true)
public List<WorkflowExecution> getExecutionsByUser(
        String createdBy) {

    return executionRepository
            .findByCreatedBy(
                    createdBy
            );
}

/*
 * =========================================
 * GET USER WORKFLOW EXECUTIONS
 * =========================================
 */

@Transactional(readOnly = true)
public List<WorkflowExecution> getExecutionsByWorkflowAndUser(
        Long workflowId,
        String createdBy) {

    return executionRepository
            .findByWorkflowIdAndCreatedBy(
                    workflowId,
                    createdBy
            );
}

/*
 * =========================================
 * START EXECUTION
 * =========================================
 */

public WorkflowExecution startExecution(
        Long executionId,
        String updatedBy) {

    WorkflowExecution execution =
            getExecutionById(
                    executionId,
                    updatedBy
            );

    ExecutionStatus currentStatus =
            execution.getStatus();

    if (!currentStatus.canTransitionTo(
            ExecutionStatus.RUNNING)) {

        throw new IllegalStateException(
                "Execution cannot transition from "
                        + currentStatus
                        + " to RUNNING"
        );
    }

    execution.setStatus(
            ExecutionStatus.RUNNING
    );

    execution.setStartedAt(
            LocalDateTime.now()
    );

    execution.setUpdatedBy(
            updatedBy
    );

    WorkflowExecution savedExecution =
            executionRepository.save(
                    execution
            );

    /*
     * =========================================
     * CREATE WORKFLOW STARTED NOTIFICATION
     * =========================================
     */

    User user =
            userRepository
                    .findByEmail(updatedBy)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found"
                            ));

    Workflow workflow =
            savedExecution.getWorkflow();

    notificationService.createNotification(
            "Workflow started",
            workflow.getName()
                    + " was started successfully.",
            NotificationType.WORKFLOW_STARTED,
            NotificationPriority.SUCCESS,
            user,
            workflow
    );

    return savedExecution;
}

/*
 * =========================================
 * COMPLETE EXECUTION
 * =========================================
 */

public WorkflowExecution completeExecution(
        Long executionId,
        String updatedBy) {

    WorkflowExecution execution =
            getExecutionById(
                    executionId,
                    updatedBy
            );

    ExecutionStatus currentStatus =
            execution.getStatus();

    if (!currentStatus.canTransitionTo(
            ExecutionStatus.COMPLETED)) {

        throw new IllegalStateException(
                "Execution cannot transition from "
                        + currentStatus
                        + " to COMPLETED"
        );
    }

    execution.setStatus(
            ExecutionStatus.COMPLETED
    );

    execution.setCompletedAt(
            LocalDateTime.now()
    );

    execution.setUpdatedBy(
            updatedBy
    );

    WorkflowExecution savedExecution =
            executionRepository.save(
                    execution
            );

    /*
     * =========================================
     * CREATE WORKFLOW COMPLETED NOTIFICATION
     * =========================================
     */

    User user =
            userRepository
                    .findByEmail(updatedBy)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found"
                            ));

    Workflow workflow =
            savedExecution.getWorkflow();

    notificationService.createNotification(
            "Workflow completed",
            workflow.getName()
                    + " was completed successfully.",
            NotificationType.WORKFLOW_COMPLETED,
            NotificationPriority.SUCCESS,
            user,
            workflow
    );

    return savedExecution;
}

/*
 * =========================================
 * FAIL EXECUTION
 * =========================================
 */

public WorkflowExecution failExecution(
        Long executionId,
        String errorMessage,
        String updatedBy) {

    WorkflowExecution execution =
            getExecutionById(
                    executionId,
                    updatedBy
            );

    ExecutionStatus currentStatus =
            execution.getStatus();

    if (!currentStatus.canTransitionTo(
            ExecutionStatus.FAILED)) {

        throw new IllegalStateException(
                "Execution cannot transition from "
                        + currentStatus
                        + " to FAILED"
        );
    }

    execution.setStatus(
            ExecutionStatus.FAILED
    );

    execution.setCompletedAt(
            LocalDateTime.now()
    );

    execution.setErrorMessage(
            errorMessage
    );

    execution.setUpdatedBy(
            updatedBy
    );

    WorkflowExecution savedExecution =
            executionRepository.save(
                    execution
            );

    /*
     * =========================================
     * CREATE WORKFLOW FAILED NOTIFICATION
     * =========================================
     */

    User user =
            userRepository
                    .findByEmail(updatedBy)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found"
                            ));

    Workflow workflow =
            savedExecution.getWorkflow();

    notificationService.createNotification(
            "Workflow failed",
            workflow.getName()
                    + " failed during execution.",
            NotificationType.WORKFLOW_FAILED,
            NotificationPriority.ERROR,
            user,
            workflow
    );

    return savedExecution;
}

/*
 * =========================================
 * CANCEL EXECUTION
 * =========================================
 */

public WorkflowExecution cancelExecution(
        Long executionId,
        String updatedBy) {

    WorkflowExecution execution =
            getExecutionById(
                    executionId,
                    updatedBy
            );

    ExecutionStatus currentStatus =
            execution.getStatus();

    if (!currentStatus.canTransitionTo(
            ExecutionStatus.CANCELLED)) {

        throw new IllegalStateException(
                "Execution cannot transition from "
                        + currentStatus
                        + " to CANCELLED"
        );
    }

    execution.setStatus(
            ExecutionStatus.CANCELLED
    );

    execution.setCompletedAt(
            LocalDateTime.now()
    );

    execution.setUpdatedBy(
            updatedBy
    );

    WorkflowExecution savedExecution =
            executionRepository.save(
                    execution
            );

    /*
     * =========================================
     * CREATE WORKFLOW CANCELLED NOTIFICATION
     * =========================================
     */

    User user =
            userRepository
                    .findByEmail(updatedBy)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found"
                            ));

    Workflow workflow =
            savedExecution.getWorkflow();

    notificationService.createNotification(
            "Workflow cancelled",
            workflow.getName()
                    + " was cancelled successfully.",
            NotificationType.WORKFLOW_CANCELLED,
            NotificationPriority.WARNING,
            user,
            workflow
    );

    return savedExecution;
}

/*
 * =========================================
 * RETRY EXECUTION
 * =========================================
 */

public WorkflowExecution retryExecution(
        Long executionId,
        String createdBy) {

    WorkflowExecution failedExecution =
            getExecutionById(
                    executionId,
                    createdBy
            );

    /*
     * =====================================
     * STATUS CHECK
     * =====================================
     */

    if (failedExecution.getStatus()
            != ExecutionStatus.FAILED) {

        throw new IllegalStateException(
                "Only failed executions can be retried"
        );
    }

    /*
     * =====================================
     * CREATE NEW EXECUTION
     * =====================================
     */

    WorkflowExecution retryExecution =
            new WorkflowExecution(
                    failedExecution.getWorkflow(),
                    createdBy
            );

    retryExecution.setStatus(
            ExecutionStatus.QUEUED
    );

    WorkflowExecution savedRetryExecution =
            executionRepository.save(
                    retryExecution
            );

    /*
     * =========================================
     * CREATE WORKFLOW RETRIED NOTIFICATION
     * =========================================
     */

    User user =
            userRepository
                    .findByEmail(createdBy)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found"
                            ));

    Workflow workflow =
            savedRetryExecution.getWorkflow();

    notificationService.createNotification(
            "Workflow retry queued",
            workflow.getName()
                    + " has been queued for retry.",
            NotificationType.WORKFLOW_RETRIED,
            NotificationPriority.INFO,
            user,
            workflow
    );

    return savedRetryExecution;
}


}
