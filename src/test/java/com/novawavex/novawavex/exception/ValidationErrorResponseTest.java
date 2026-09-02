package com.novawavex.novawavex.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationErrorResponseTest {


// =========================================================
// 1. DEFAULT CONSTRUCTOR
// =========================================================

@Test
void defaultConstructor_shouldCreateObject() {

    ValidationErrorResponse response =
            new ValidationErrorResponse();

    assertNotNull(response);

    assertNull(response.getTimestamp());

    assertEquals(
            0,
            response.getStatus()
    );

    assertNull(response.getMessage());

    assertNull(response.getErrors());
}


// =========================================================
// 2. PARAMETERIZED CONSTRUCTOR
// =========================================================

@Test
void parameterizedConstructor_shouldSetAllFields() {

    LocalDateTime timestamp =
            LocalDateTime.now();

    Map<String, String> errors =
            new LinkedHashMap<>();

    errors.put(
            "email",
            "Email must be valid"
    );

    errors.put(
            "password",
            "Password is required"
    );

    ValidationErrorResponse response =
            new ValidationErrorResponse(
                    timestamp,
                    400,
                    "Validation failed",
                    errors
            );

    assertEquals(
            timestamp,
            response.getTimestamp()
    );

    assertEquals(
            400,
            response.getStatus()
    );

    assertEquals(
            "Validation failed",
            response.getMessage()
    );

    assertEquals(
            errors,
            response.getErrors()
    );
}


// =========================================================
// 3. EMPTY ERRORS
// =========================================================

@Test
void parameterizedConstructor_withEmptyErrors_shouldPreserveEmptyMap() {

    LocalDateTime timestamp =
            LocalDateTime.now();

    Map<String, String> errors =
            new LinkedHashMap<>();

    ValidationErrorResponse response =
            new ValidationErrorResponse(
                    timestamp,
                    400,
                    "Validation failed",
                    errors
            );

    assertNotNull(
            response.getErrors()
    );

    assertTrue(
            response.getErrors().isEmpty()
    );
}


// =========================================================
// 4. GETTERS SHOULD_RETURN_UPDATED_REFERENCE
// =========================================================

@Test
void getters_shouldReturnProvidedValues() {

    LocalDateTime timestamp =
            LocalDateTime.of(
                    2026,
                    8,
                    28,
                    19,
                    0
            );

    Map<String, String> errors =
            new LinkedHashMap<>();

    errors.put(
            "name",
            "Name is required"
    );

    ValidationErrorResponse response =
            new ValidationErrorResponse(
                    timestamp,
                    422,
                    "Invalid request",
                    errors
            );

    assertSame(
            timestamp,
            response.getTimestamp()
    );

    assertSame(
            errors,
            response.getErrors()
    );

    assertEquals(
            422,
            response.getStatus()
    );

    assertEquals(
            "Invalid request",
            response.getMessage()
    );
}

}
