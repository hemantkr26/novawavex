package com.novawavex.novawavex.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {


// =========================================================
// 1. MESSAGE
// =========================================================

@Test
void constructor_shouldStoreMessage() {

    UnauthorizedException exception =
            new UnauthorizedException(
                    "Authentication required"
            );

    assertEquals(
            "Authentication required",
            exception.getMessage()
    );
}


// =========================================================
// 2. RUNTIME EXCEPTION
// =========================================================

@Test
void exception_shouldExtendRuntimeException() {

    UnauthorizedException exception =
            new UnauthorizedException(
                    "Unauthorized access"
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

    UnauthorizedException exception =
            new UnauthorizedException(null);

    assertNull(
            exception.getMessage()
    );
}


}
