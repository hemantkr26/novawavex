package com.novawavex.novawavex.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    Optional<Workflow> findByIdAndCreatedBy(
            Long id,
            String createdBy
    );

    List<Workflow> findByCreatedBy(
            String createdBy
    );
}