
package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.dto.WorkflowResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationService;
import com.novawavex.novawavex.notification.NotificationType;
import com.novawavex.novawavex.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    private final WorkflowExecutionRepository executionRepository;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    public WorkflowService(
            WorkflowRepository workflowRepository,
            WorkflowExecutionRepository executionRepository,
            NotificationService notificationService,
            UserRepository userRepository) {

        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // =========================================
    // CREATE WORKFLOW
    // =========================================

    @Transactional
    public WorkflowResponse createWorkflow(
            WorkflowRequest request,
            String createdBy) {

        Workflow workflow = new Workflow(
                request.getName(),
                request.getDescription(),
                request.getStatus(),
                createdBy
        );

        Workflow savedWorkflow =
                workflowRepository.save(workflow);

        User user =
                userRepository
                        .findByEmail(createdBy)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        notificationService.createNotification(
                "Workflow created",
                savedWorkflow.getName()
                        + " was created successfully.",
                NotificationType.WORKFLOW_CREATED,
                NotificationPriority.SUCCESS,
                user,
                savedWorkflow
        );

        return convertToResponse(savedWorkflow);
    }

    // =========================================
    // GET ALL WORKFLOWS
    // =========================================

    @Transactional(readOnly = true)
    public List<WorkflowResponse> getAllWorkflows(
            String createdBy) {

        return workflowRepository
                .findByCreatedBy(createdBy)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // =========================================
    // GET WORKFLOW BY ID
    // =========================================

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowById(
            Long id,
            String createdBy) {

        Workflow workflow =
                workflowRepository
                        .findByIdAndCreatedBy(
                                id,
                                createdBy
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workflow not found"
                                ));

        return convertToResponse(workflow);
    }

    // =========================================
    // UPDATE WORKFLOW
    // =========================================

    @Transactional
    public WorkflowResponse updateWorkflow(
            Long id,
            WorkflowRequest request,
            String createdBy) {

        Workflow existingWorkflow =
                workflowRepository
                        .findByIdAndCreatedBy(
                                id,
                                createdBy
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workflow not found"
                                ));

        existingWorkflow.setName(
                request.getName()
        );

        existingWorkflow.setDescription(
                request.getDescription()
        );

        existingWorkflow.setStatus(
                request.getStatus()
        );

        Workflow updatedWorkflow =
                workflowRepository.save(
                        existingWorkflow
                );

        User user =
                userRepository
                        .findByEmail(createdBy)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        notificationService.createNotification(
                "Workflow updated",
                updatedWorkflow.getName()
                        + " was updated successfully.",
                NotificationType.WORKFLOW_UPDATED,
                NotificationPriority.SUCCESS,
                user,
                updatedWorkflow
        );

        return convertToResponse(updatedWorkflow);
    }

    // =========================================
    // DELETE WORKFLOW
    // =========================================

    @Transactional
    public void deleteWorkflow(
            Long id,
            String createdBy) {

        /*
         * =========================================
         * FIND WORKFLOW
         * =========================================
         *
         * The workflow must belong to the currently
         * authenticated user.
         */

        Workflow workflow =
                workflowRepository
                        .findByIdAndCreatedBy(
                                id,
                                createdBy
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workflow not found"
                                ));

        /*
         * =========================================
         * FIND USER
         * =========================================
         */

        User user =
                userRepository
                        .findByEmail(createdBy)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        String workflowName =
                workflow.getName();

        /*
         * =========================================
         * DETACH NOTIFICATIONS
         * =========================================
         *
         * Notifications may contain a foreign-key
         * reference to this workflow.
         *
         * We detach them first so that deleting the
         * workflow does not violate the notification
         * foreign-key constraint.
         */

        notificationService.detachNotificationsFromWorkflow(
                id
        );

        /*
         * =========================================
         * DELETE WORKFLOW EXECUTIONS
         * =========================================
         *
         * WorkflowExecution has a mandatory
         * workflow_id foreign-key reference.
         *
         * Therefore PostgreSQL will reject the
         * workflow deletion while executions still
         * reference it.
         *
         * Delete those executions first.
         */

        List<WorkflowExecution> executions =
                executionRepository.findByWorkflowId(
                        id
                );

        if (!executions.isEmpty()) {

            executionRepository.deleteAll(
                    executions
            );

            executionRepository.flush();
        }

        /*
         * =========================================
         * CREATE DELETION NOTIFICATION
         * =========================================
         *
         * The workflow reference is intentionally
         * null because the workflow is about to be
         * deleted.
         */

        notificationService.createNotification(
                "Workflow deleted",
                workflowName
                        + " was deleted successfully.",
                NotificationType.WORKFLOW_DELETED,
                NotificationPriority.SUCCESS,
                user,
                null
        );

        /*
         * =========================================
         * DELETE WORKFLOW
         * =========================================
         */

        workflowRepository.delete(
                workflow
        );
    }

    // =========================================
    // CONVERT TO RESPONSE
    // =========================================

    private WorkflowResponse convertToResponse(
            Workflow workflow) {

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getDescription(),
                workflow.getStatus(),
                workflow.getCreatedBy(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt()
        );
    }
}

