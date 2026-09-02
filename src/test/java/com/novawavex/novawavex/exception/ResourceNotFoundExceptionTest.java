package com.novawavex.novawavex.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {


// =========================================================
// 1. MESSAGE
// =========================================================

@Test
void constructor_shouldStoreMessage() {

    ResourceNotFoundException exception =
            new ResourceNotFoundException(
                    "Workflow not found"
            );

    assertEquals(
            "Workflow not found",
            exception.getMessage()
    );
}


// =========================================================
// 2. RUNTIME EXCEPTION
// =========================================================

@Test
void exception_shouldExtendRuntimeException() {

    ResourceNotFoundException exception =
            new ResourceNotFoundException(
                    "Resource not found"
            );

    assertInstanceOf(
            RuntimeException.class,
            exception
    );
}


// =========================================================
// 3. NULL MESSAGE
// =========================================================

@Test
void constructor_withNullMessage_shouldAllowNull() {

    ResourceNotFoundException exception =
            new ResourceNotFoundException(null);

    assertNull(
            exception.getMessage()
    );
}


}
