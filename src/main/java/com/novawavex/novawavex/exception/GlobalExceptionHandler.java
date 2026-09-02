
package com.novawavex.novawavex.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * =========================================
     * 400 - VALIDATION ERROR
     * =========================================
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        "Validation failed",
                        request.getRequestURI(),
                        errors
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /*
     * =========================================
     * 400 - MALFORMED JSON
     * =========================================
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Malformed or unreadable request body",
                request
        );
    }


    /*
     * =========================================
     * 400 - INVALID PATH VARIABLE / PARAMETER
     * =========================================
     */

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid value for parameter: "
                        + exception.getName(),
                request
        );
    }


    /*
     * =========================================
     * 400 - INVALID BUSINESS STATE
     * =========================================
     *
     * Used when a valid request cannot be
     * performed because the resource is in
     * an invalid state.
     *
     * Example:
     *
     * QUEUED -> COMPLETED
     *
     * This is a client/business-rule error,
     * not an unexpected server failure.
     */

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage(),
                request
        );
    }


    /*
     * =========================================
     * 401 - UNAUTHORIZED
     * =========================================
     */

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                exception.getMessage(),
                request
        );
    }


    /*
     * =========================================
     * 404 - RESOURCE NOT FOUND
     * =========================================
     */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                exception.getMessage(),
                request
        );
    }


    /*
     * =========================================
     * 404 - UNKNOWN ENDPOINT
     * =========================================
     *
     * Spring Boot 4 / Spring Framework 7 can
     * throw NoResourceFoundException when an
     * endpoint does not exist.
     */

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "The requested endpoint was not found",
                request
        );
    }


    /*
     * =========================================
     * 404 - LEGACY UNKNOWN ENDPOINT HANDLING
     * =========================================
     */

    @ExceptionHandler(
            org.springframework.web.servlet.NoHandlerFoundException.class
    )
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "The requested endpoint was not found",
                request
        );
    }


    /*
     * =========================================
     * 409 - DUPLICATE RESOURCE
     * =========================================
     */

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(
            DuplicateResourceException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                exception.getMessage(),
                request
        );
    }


    /*
     * =========================================
     * 405 - HTTP METHOD NOT SUPPORTED
     * =========================================
     */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                "HTTP method "
                        + exception.getMethod()
                        + " is not supported for this endpoint",
                request
        );
    }


    /*
     * =========================================
     * 415 - UNSUPPORTED MEDIA TYPE
     * =========================================
     */

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported Media Type",
                "The request content type is not supported",
                request
        );
    }


    /*
     * =========================================
     * 404 - ILLEGAL ARGUMENT
     * =========================================
     *
     * Existing NovaWavex services use
     * IllegalArgumentException for some
     * nonexistent resources.
     *
     * Treat the resource-not-found cases as
     * 404 instead of exposing them as 500.
     */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                exception.getMessage(),
                request
        );
    }


    /*
     * =========================================
     * 500 - UNEXPECTED SERVER ERROR
     * =========================================
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request
        );
    }


    /*
     * =========================================
     * STANDARD ERROR RESPONSE BUILDER
     * =========================================
     */

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        error,
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

