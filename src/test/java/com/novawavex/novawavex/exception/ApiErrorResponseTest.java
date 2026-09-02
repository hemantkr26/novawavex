package com.novawavex.novawavex.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {


@Test
void defaultConstructor_shouldCreateObject() {

    ApiErrorResponse response =
            new ApiErrorResponse();

    assertNotNull(response);
    assertNull(response.getTimestamp());
    assertEquals(0, response.getStatus());
    assertNull(response.getError());
    assertNull(response.getMessage());
    assertNull(response.getPath());
    assertNull(response.getErrors());
}

@Test
void constructorWithoutErrors_shouldSetAllFields() {

    LocalDateTime timestamp =
            LocalDateTime.now();

    ApiErrorResponse response =
            new ApiErrorResponse(
                    timestamp,
                    404,
                    "Not Found",
                    "Resource not found",
                    "/api/test"
            );

    assertEquals(timestamp, response.getTimestamp());
    assertEquals(404, response.getStatus());
    assertEquals("Not Found", response.getError());
    assertEquals(
            "Resource not found",
            response.getMessage()
    );
    assertEquals(
            "/api/test",
            response.getPath()
    );
    assertNull(response.getErrors());
}

@Test
void constructorWithErrors_shouldSetAllFields() {

    LocalDateTime timestamp =
            LocalDateTime.now();

    Map<String, String> errors =
            new LinkedHashMap<>();

    errors.put("email", "Invalid email");
    errors.put("password", "Password is required");

    ApiErrorResponse response =
            new ApiErrorResponse(
                    timestamp,
                    400,
                    "Bad Request",
                    "Validation failed",
                    "/api/auth/register",
                    errors
            );

    assertEquals(timestamp, response.getTimestamp());
    assertEquals(400, response.getStatus());
    assertEquals("Bad Request", response.getError());
    assertEquals(
            "Validation failed",
            response.getMessage()
    );
    assertEquals(
            "/api/auth/register",
            response.getPath()
    );
    assertEquals(errors, response.getErrors());
}

@Test
void setters_shouldUpdateAllFields() {

    ApiErrorResponse response =
            new ApiErrorResponse();

    LocalDateTime timestamp =
            LocalDateTime.now();

    Map<String, String> errors =
            new LinkedHashMap<>();

    errors.put("name", "Name is required");

    response.setTimestamp(timestamp);
    response.setStatus(400);
    response.setError("Bad Request");
    response.setMessage("Validation failed");
    response.setPath("/api/users");
    response.setErrors(errors);

    assertEquals(timestamp, response.getTimestamp());
    assertEquals(400, response.getStatus());
    assertEquals(
            "Bad Request",
            response.getError()
    );
    assertEquals(
            "Validation failed",
            response.getMessage()
    );
    assertEquals(
            "/api/users",
            response.getPath()
    );
    assertEquals(errors, response.getErrors());
}

@Test
void errorsMap_shouldPreserveMultipleValidationErrors() {

    Map<String, String> errors =
            new LinkedHashMap<>();

    errors.put("email", "Invalid email");
    errors.put("password", "Password is required");
    errors.put("fullName", "Name is required");

    ApiErrorResponse response =
            new ApiErrorResponse(
                    LocalDateTime.now(),
                    400,
                    "Bad Request",
                    "Validation failed",
                    "/api/auth/register",
                    errors
            );

    assertNotNull(response.getErrors());
    assertEquals(3, response.getErrors().size());
    assertEquals(
            "Invalid email",
            response.getErrors().get("email")
    );
    assertEquals(
            "Password is required",
            response.getErrors().get("password")
    );
    assertEquals(
            "Name is required",
            response.getErrors().get("fullName")
    );
}

}
