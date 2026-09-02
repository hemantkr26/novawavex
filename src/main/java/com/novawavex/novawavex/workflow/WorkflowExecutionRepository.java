package com.novawavex.novawavex.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowExecutionRepository
extends JpaRepository<WorkflowExecution, Long> {


Optional<WorkflowExecution> findByIdAndCreatedBy(
        Long id,
        String createdBy
);

List<WorkflowExecution> findByWorkflowId(
        Long workflowId
);

List<WorkflowExecution> findByCreatedBy(
        String createdBy
);

List<WorkflowExecution> findByWorkflowIdAndCreatedBy(
        Long workflowId,
        String createdBy
);


}
