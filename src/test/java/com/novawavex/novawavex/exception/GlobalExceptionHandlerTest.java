package com.novawavex.novawavex.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {


private final GlobalExceptionHandler handler =
        new GlobalExceptionHandler();


// =========================================================
// 1. UNAUTHORIZED - 401
// =========================================================

@Test
void handleUnauthorized_shouldReturn401() {

    HttpServletRequest request =
            mock(HttpServletRequest.class);

    when(request.getRequestURI())
            .thenReturn("/api/auth/test");

    UnauthorizedException exception =
            new UnauthorizedException(
                    "Authentication required"
            );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleUnauthorized(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.UNAUTHORIZED,
            response.getStatusCode()
    );

    assertNotNull(
            response.getBody()
    );

    assertEquals(
            401,
            response.getBody().getStatus()
    );

    assertEquals(
            "Unauthorized",
            response.getBody().getError()
    );

    assertEquals(
            "Authentication required",
            response.getBody().getMessage()
    );

    assertEquals(
            "/api/auth/test",
            response.getBody().getPath()
    );

    assertNotNull(
            response.getBody().getTimestamp()
    );
}


// =========================================================
// 2. RESOURCE NOT FOUND - 404
// =========================================================

@Test
void handleResourceNotFound_shouldReturn404() {

    HttpServletRequest request =
            mock(HttpServletRequest.class);

    when(request.getRequestURI())
            .thenReturn("/api/workflows/999");

    ResourceNotFoundException exception =
            new ResourceNotFoundException(
                    "Workflow not found"
            );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleResourceNotFound(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.NOT_FOUND,
            response.getStatusCode()
    );

    assertNotNull(
            response.getBody()
    );

    assertEquals(
            404,
            response.getBody().getStatus()
    );

    assertEquals(
            "Not Found",
            response.getBody().getError()
    );

    assertEquals(
            "Workflow not found",
            response.getBody().getMessage()
    );

    assertEquals(
            "/api/workflows/999",
            response.getBody().getPath()
    );

    assertNotNull(
            response.getBody().getTimestamp()
    );
}


// =========================================================
// 3. DUPLICATE RESOURCE - 409
// =========================================================

@Test
void handleDuplicateResource_shouldReturn409() {

    HttpServletRequest request =
            mock(HttpServletRequest.class);

    when(request.getRequestURI())
            .thenReturn("/api/auth/register");

    DuplicateResourceException exception =
            new DuplicateResourceException(
                    "Email already exists"
            );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleDuplicateResource(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.CONFLICT,
            response.getStatusCode()
    );

    assertNotNull(
            response.getBody()
    );

    assertEquals(
            409,
            response.getBody().getStatus()
    );

    assertEquals(
            "Conflict",
            response.getBody().getError()
    );

    assertEquals(
            "Email already exists",
            response.getBody().getMessage()
    );

    assertEquals(
            "/api/auth/register",
            response.getBody().getPath()
    );

    assertNotNull(
            response.getBody().getTimestamp()
    );
}


// =========================================================
// 4. GENERAL EXCEPTION - 500
// =========================================================

@Test
void handleGeneralException_shouldReturn500() {

    HttpServletRequest request =
            mock(HttpServletRequest.class);

    when(request.getRequestURI())
            .thenReturn("/api/test");

    Exception exception =
            new RuntimeException(
                    "Unexpected database failure"
            );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleGeneralException(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            response.getStatusCode()
    );

    assertNotNull(
            response.getBody()
    );

    assertEquals(
            500,
            response.getBody().getStatus()
    );

    assertEquals(
            "Internal Server Error",
            response.getBody().getError()
    );

    assertEquals(
            "An unexpected error occurred",
            response.getBody().getMessage()
    );

    assertEquals(
            "/api/test",
            response.getBody().getPath()
    );

    assertNotNull(
            response.getBody().getTimestamp()
    );
}


// =========================================================
// 5. VALIDATION ERROR - 400
// =========================================================

@Test
void handleValidationErrors_shouldReturn400WithFieldErrors() {

    HttpServletRequest request =
            mock(HttpServletRequest.class);

    when(request.getRequestURI())
            .thenReturn("/api/auth/register");

    MethodArgumentNotValidException exception =
            mock(MethodArgumentNotValidException.class);

    BindingResult bindingResult =
            mock(BindingResult.class);

    FieldError emailError =
            new FieldError(
                    "registerRequest",
                    "email",
                    "Email must be valid"
            );

    FieldError passwordError =
            new FieldError(
                    "registerRequest",
                    "password",
                    "Password is required"
            );

    when(exception.getBindingResult())
            .thenReturn(bindingResult);

    when(bindingResult.getFieldErrors())
            .thenReturn(
                    java.util.List.of(
                            emailError,
                            passwordError
                    )
            );

    ResponseEntity<ApiErrorResponse> response =
            handler.handleValidationErrors(
                    exception,
                    request
            );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            response.getStatusCode()
    );

    assertNotNull(
            response.getBody()
    );

    assertEquals(
            400,
            response.getBody().getStatus()
    );

    assertEquals(
            "Bad Request",
            response.getBody().getError()
    );

    assertEquals(
            "Validation failed",
            response.getBody().getMessage()
    );

    assertEquals(
            "/api/auth/register",
            response.getBody().getPath()
    );

    assertNotNull(
            response.getBody().getTimestamp()
    );

    Map<String, String> errors =
            response.getBody().getErrors();

    assertNotNull(errors);

    assertEquals(
            2,
            errors.size()
    );

    assertEquals(
            "Email must be valid",
            errors.get("email")
    );

    assertEquals(
            "Password is required",
            errors.get("password")
    );
}


}
