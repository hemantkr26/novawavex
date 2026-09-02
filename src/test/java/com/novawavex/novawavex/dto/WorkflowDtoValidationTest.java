
package com.novawavex.novawavex.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.novawavex.novawavex.workflow.WorkflowStatus;

class WorkflowDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {

        validatorFactory.close();
    }

    // =========================================================
    // VALID WORKFLOW REQUEST
    // =========================================================

    @Test
    void workflowRequest_withValidData_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Test Workflow");
        request.setDescription("Test workflow description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    // =========================================================
    // WORKFLOW NAME
    // =========================================================

    @Test
    void workflowRequest_withNullName_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(null);
        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Workflow name is required"
                                        )
                        )
        );
    }

    @Test
    void workflowRequest_withBlankName_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("");
        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Workflow name is required"
                                        )
                        )
        );
    }

    @Test
    void workflowRequest_withWhitespaceName_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("   ");
        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Workflow name is required"
                                        )
                        )
        );
    }

    @Test
    void workflowRequest_withExactly150CharacterName_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "A".repeat(150)
        );

        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void workflowRequest_withNameOver150Characters_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName(
                "A".repeat(151)
        );

        request.setDescription("Test description");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Workflow name must not exceed 150 characters"
                                        )
                        )
        );
    }

    // =========================================================
    // DESCRIPTION
    // =========================================================

    @Test
    void workflowRequest_withNullDescription_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Test Workflow");
        request.setDescription(null);
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void workflowRequest_withDescriptionOver1000Characters_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Test Workflow");

        request.setDescription(
                "A".repeat(1001)
        );

        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Description must not exceed 1000 characters"
                                        )
                        )
        );
    }

    @Test
    void workflowRequest_withExactly1000CharacterDescription_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Test Workflow");

        request.setDescription(
                "A".repeat(1000)
        );

        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    // =========================================================
    // WORKFLOW STATUS
    // =========================================================

    @Test
    void workflowRequest_withNullStatus_shouldFailValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Test Workflow");
        request.setDescription("Test description");
        request.setStatus(null);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getMessage()
                                        .equals(
                                                "Workflow status is required"
                                        )
                        )
        );
    }

    @Test
    void workflowRequest_withDraftStatus_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Draft Workflow");
        request.setDescription("Draft workflow");
        request.setStatus(WorkflowStatus.DRAFT);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void workflowRequest_withActiveStatus_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Active Workflow");
        request.setDescription("Active workflow");
        request.setStatus(WorkflowStatus.ACTIVE);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void workflowRequest_withCompletedStatus_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Completed Workflow");
        request.setDescription("Completed workflow");
        request.setStatus(WorkflowStatus.COMPLETED);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    @Test
    void workflowRequest_withCancelledStatus_shouldPassValidation() {

        WorkflowRequest request =
                new WorkflowRequest();

        request.setName("Cancelled Workflow");
        request.setDescription("Cancelled workflow");
        request.setStatus(WorkflowStatus.CANCELLED);

        Set<ConstraintViolation<WorkflowRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty()
        );
    }

    // =========================================================
    // WORKFLOW RESPONSE
    // =========================================================

    @Test
    void workflowResponse_defaultConstructor_shouldHaveNullValues() {

        WorkflowResponse response =
                new WorkflowResponse();

        assertEquals(
                null,
                response.getId()
        );

        assertEquals(
                null,
                response.getName()
        );

        assertEquals(
                null,
                response.getDescription()
        );

        assertEquals(
                null,
                response.getStatus()
        );

        assertEquals(
                null,
                response.getCreatedBy()
        );

        assertEquals(
                null,
                response.getCreatedAt()
        );

        assertEquals(
                null,
                response.getUpdatedAt()
        );
    }

    @Test
    void workflowResponse_constructor_shouldMapAllValues() {

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        28,
                        10,
                        30
                );

        LocalDateTime updatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        28,
                        11,
                        30
                );

        WorkflowResponse response =
                new WorkflowResponse(
                        1L,
                        "Test Workflow",
                        "Test Description",
                        WorkflowStatus.ACTIVE,
                        "user@example.com",
                        createdAt,
                        updatedAt
                );

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "Test Workflow",
                response.getName()
        );

        assertEquals(
                "Test Description",
                response.getDescription()
        );

        assertEquals(
                WorkflowStatus.ACTIVE,
                response.getStatus()
        );

        assertEquals(
                "user@example.com",
                response.getCreatedBy()
        );

        assertEquals(
                createdAt,
                response.getCreatedAt()
        );

        assertEquals(
                updatedAt,
                response.getUpdatedAt()
        );
    }
}

