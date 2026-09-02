package com.novawavex.novawavex.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateResourceExceptionTest {


// =========================================================
// 1. MESSAGE
// =========================================================

@Test
void constructor_shouldStoreMessage() {

    DuplicateResourceException exception =
            new DuplicateResourceException(
                    "User already exists"
            );

    assertEquals(
            "User already exists",
            exception.getMessage()
    );
}


// =========================================================
// 2. RUNTIME EXCEPTION
// =========================================================

@Test
void exception_shouldExtendRuntimeException() {

    DuplicateResourceException exception =
            new DuplicateResourceException(
                    "Duplicate resource"
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

    DuplicateResourceException exception =
            new DuplicateResourceException(null);

    assertNull(
            exception.getMessage()
    );
}


}
