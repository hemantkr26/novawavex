
package com.novawavex.novawavex.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class WorkflowRepositoryTest {

    @Autowired
    private WorkflowRepository workflowRepository;


    // =========================================================
    // 1. SAVE WORKFLOW
    // =========================================================

    @Test
    void saveWorkflow_shouldPersistWorkflow() {

        Workflow workflow = new Workflow();

        workflow.setName("Repository Save Workflow");
        workflow.setDescription("Workflow repository save test");
        workflow.setStatus(WorkflowStatus.DRAFT);
        workflow.setCreatedBy(
                "workflow-save@novawavex.com"
        );

        Workflow savedWorkflow =
                workflowRepository.save(workflow);

        assertNotNull(
                savedWorkflow.getId()
        );

        assertEquals(
                "Repository Save Workflow",
                savedWorkflow.getName()
        );

        assertEquals(
                "workflow-save@novawavex.com",
                savedWorkflow.getCreatedBy()
        );

        assertEquals(
                WorkflowStatus.DRAFT,
                savedWorkflow.getStatus()
        );
    }


    // =========================================================
    // 2. FIND BY CREATED BY
    // =========================================================

    @Test
    void findByCreatedBy_shouldReturnOnlyUserWorkflows() {

        Workflow userAWorkflow1 =
                new Workflow();

        userAWorkflow1.setName(
                "User A Workflow 1"
        );

        userAWorkflow1.setDescription(
                "First User A workflow"
        );

        userAWorkflow1.setStatus(
                WorkflowStatus.DRAFT
        );

        userAWorkflow1.setCreatedBy(
                "user-a@novawavex.com"
        );


        Workflow userAWorkflow2 =
                new Workflow();

        userAWorkflow2.setName(
                "User A Workflow 2"
        );

        userAWorkflow2.setDescription(
                "Second User A workflow"
        );

        userAWorkflow2.setStatus(
                WorkflowStatus.ACTIVE
        );

        userAWorkflow2.setCreatedBy(
                "user-a@novawavex.com"
        );


        Workflow userBWorkflow =
                new Workflow();

        userBWorkflow.setName(
                "User B Workflow"
        );

        userBWorkflow.setDescription(
                "User B workflow"
        );

        userBWorkflow.setStatus(
                WorkflowStatus.DRAFT
        );

        userBWorkflow.setCreatedBy(
                "user-b@novawavex.com"
        );


        workflowRepository.save(
                userAWorkflow1
        );

        workflowRepository.save(
                userAWorkflow2
        );

        workflowRepository.save(
                userBWorkflow
        );


        List<Workflow> result =
                workflowRepository.findByCreatedBy(
                        "user-a@novawavex.com"
                );


        assertEquals(
                2,
                result.size()
        );


        assertTrue(
                result.stream()
                        .allMatch(workflow ->
                                workflow.getCreatedBy()
                                        .equals(
                                                "user-a@novawavex.com"
                                        )
                        )
        );
    }


    // =========================================================
    // 3. FIND BY CREATED BY - NO RESULTS
    // =========================================================

    @Test
    void findByCreatedBy_whenUserHasNoWorkflows_shouldReturnEmptyList() {

        List<Workflow> result =
                workflowRepository.findByCreatedBy(
                        "missing-user@novawavex.com"
                );

        assertNotNull(
                result
        );

        assertTrue(
                result.isEmpty()
        );
    }


    // =========================================================
    // 4. FIND BY ID AND CREATED BY - OWNER
    // =========================================================

    @Test
    void findByIdAndCreatedBy_whenOwnerMatches_shouldReturnWorkflow() {

        Workflow workflow =
                new Workflow();

        workflow.setName(
                "Owner Access Workflow"
        );

        workflow.setDescription(
                "Ownership repository test"
        );

        workflow.setStatus(
                WorkflowStatus.ACTIVE
        );

        workflow.setCreatedBy(
                "owner@novawavex.com"
        );


        Workflow savedWorkflow =
                workflowRepository.save(
                        workflow
                );


        Optional<Workflow> result =
                workflowRepository.findByIdAndCreatedBy(
                        savedWorkflow.getId(),
                        "owner@novawavex.com"
                );


        assertTrue(
                result.isPresent()
        );

        assertEquals(
                savedWorkflow.getId(),
                result.get().getId()
        );

        assertEquals(
                "owner@novawavex.com",
                result.get().getCreatedBy()
        );
    }


    // =========================================================
    // 5. FIND BY ID AND CREATED BY - WRONG USER
    // =========================================================

    @Test
    void findByIdAndCreatedBy_whenOwnerDoesNotMatch_shouldReturnEmpty() {

        Workflow workflow =
                new Workflow();

        workflow.setName(
                "Protected Workflow"
        );

        workflow.setDescription(
                "Ownership protection test"
        );

        workflow.setStatus(
                WorkflowStatus.DRAFT
        );

        workflow.setCreatedBy(
                "owner@novawavex.com"
        );


        Workflow savedWorkflow =
                workflowRepository.save(
                        workflow
                );


        Optional<Workflow> result =
                workflowRepository.findByIdAndCreatedBy(
                        savedWorkflow.getId(),
                        "different-user@novawavex.com"
                );


        assertFalse(
                result.isPresent()
        );
    }


    // =========================================================
    // 6. FIND BY ID AND CREATED BY - MISSING WORKFLOW
    // =========================================================

    @Test
    void findByIdAndCreatedBy_whenWorkflowDoesNotExist_shouldReturnEmpty() {

        Optional<Workflow> result =
                workflowRepository.findByIdAndCreatedBy(
                        999999L,
                        "missing-user@novawavex.com"
                );

        assertTrue(
                result.isEmpty()
        );
    }
}
