package com.novawavex.novawavex.workflow;

import com.novawavex.novawavex.dto.WorkflowRequest;
import com.novawavex.novawavex.dto.WorkflowResponse;
import com.novawavex.novawavex.workflow.WorkflowService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@SecurityRequirement(name = "bearerAuth")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowResponse createWorkflow(
            @Valid @RequestBody WorkflowRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();

        return workflowService.createWorkflow(
                request,
                createdBy
        );
    }

    @GetMapping
    public List<WorkflowResponse> getAllWorkflows(
            Authentication authentication) {

        String createdBy = authentication.getName();

        return workflowService.getAllWorkflows(
                createdBy
        );
    }

    @GetMapping("/{id}")
    public WorkflowResponse getWorkflowById(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy = authentication.getName();

        return workflowService.getWorkflowById(
                id,
                createdBy
        );
    }

    @PutMapping("/{id}")
    public WorkflowResponse updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();

        return workflowService.updateWorkflow(
                id,
                request,
                createdBy
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkflow(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy = authentication.getName();

        workflowService.deleteWorkflow(
                id,
                createdBy
        );
    }
}