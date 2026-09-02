
package com.novawavex.novawavex.workflow;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/executions")
@SecurityRequirement(name = "bearerAuth")
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    private final WorkflowRepository workflowRepository;

    public WorkflowExecutionController(
            WorkflowExecutionService executionService,
            WorkflowRepository workflowRepository) {

        this.executionService = executionService;
        this.workflowRepository = workflowRepository;
    }

    /*
     * =========================================
     * CREATE EXECUTION
     * =========================================
     */

    @PostMapping("/workflow/{workflowId}")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowExecution createExecution(
            @PathVariable Long workflowId,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        Workflow workflow =
                workflowRepository
                        .findByIdAndCreatedBy(
                                workflowId,
                                createdBy
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Workflow not found"
                                )
                        );

        return executionService.createExecution(
                workflow,
                createdBy
        );
    }

    /*
     * =========================================
     * GET EXECUTION BY ID
     * =========================================
     *
     * Ownership is enforced directly by the
     * service using findByIdAndCreatedBy().
     */

    @GetMapping("/{id}")
    public WorkflowExecution getExecutionById(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.getExecutionById(
                id,
                createdBy
        );
    }

    /*
     * =========================================
     * GET CURRENT USER EXECUTION HISTORY
     * =========================================
     */

    @GetMapping
    public List<WorkflowExecution> getMyExecutions(
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService
                .getExecutionsByUser(
                        createdBy
                );
    }

    /*
     * =========================================
     * START EXECUTION
     * =========================================
     *
     * Ownership is enforced by the service.
     */

    @PostMapping("/{id}/start")
    public WorkflowExecution startExecution(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.startExecution(
                id,
                createdBy
        );
    }

    /*
     * =========================================
     * COMPLETE EXECUTION
     * =========================================
     *
     * Ownership is enforced by the service.
     */

    @PostMapping("/{id}/complete")
    public WorkflowExecution completeExecution(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.completeExecution(
                id,
                createdBy
        );
    }

    /*
     * =========================================
     * FAIL EXECUTION
     * =========================================
     *
     * Ownership is enforced by the service.
     */

    @PostMapping("/{id}/fail")
    public WorkflowExecution failExecution(
            @PathVariable Long id,
            @RequestParam(required = false)
            String errorMessage,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.failExecution(
                id,
                errorMessage,
                createdBy
        );
    }

    /*
     * =========================================
     * CANCEL EXECUTION
     * =========================================
     *
     * Ownership is enforced by the service.
     */

    @PostMapping("/{id}/cancel")
    public WorkflowExecution cancelExecution(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.cancelExecution(
                id,
                createdBy
        );
    }

    /*
     * =========================================
     * RETRY EXECUTION
     * =========================================
     *
     * Ownership is enforced by the service.
     */

    @PostMapping("/{id}/retry")
    public WorkflowExecution retryExecution(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy =
                authentication.getName();

        return executionService.retryExecution(
                id,
                createdBy
        );
    }
}

