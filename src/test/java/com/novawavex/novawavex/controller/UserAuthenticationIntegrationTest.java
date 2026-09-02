package com.novawavex.novawavex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.AuthResponse;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthenticationIntegrationTest {


@Autowired
private MockMvc mockMvc;

@Autowired
private UserRepository userRepository;

/*
 * Create the ObjectMapper directly.
 *
 * We do NOT autowire it because the current
 * NovaWavex application context does not expose
 * an ObjectMapper as a Spring bean.
 */
private final ObjectMapper objectMapper =
        new ObjectMapper();


@BeforeEach
void cleanDatabase() {

    userRepository
            .findByEmail(
                    "t15-integration@novawavex.com"
            )
            .ifPresent(userRepository::delete);
}


// =========================================================
// T15.1
//
// REGISTER
//      ↓
// LOGIN
//      ↓
// JWT
//      ↓
// GET /api/users/me
// =========================================================

@Test
void registerLoginAndGetCurrentUser_shouldCompleteSuccessfully()
        throws Exception {

    String email =
            "t15-integration@novawavex.com";

    String password =
            "T15Test12345";

    String fullName =
            "T15 Integration Test User";


    // =====================================================
    // STEP 1 — REGISTER
    // =====================================================

    UserRequest registerRequest =
            new UserRequest();

    registerRequest.setFullName(fullName);
    registerRequest.setEmail(email);
    registerRequest.setPassword(password);


    String registerResponse =
            mockMvc.perform(
                    post("/api/users")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .content(
                                    objectMapper.writeValueAsString(
                                            registerRequest
                                    )
                            )
            )
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.fullName")
                            .value(fullName)
            )
            .andExpect(
                    jsonPath("$.email")
                            .value(email)
            )
            .andReturn()
            .getResponse()
            .getContentAsString();


    UserResponse registeredUser =
            objectMapper.readValue(
                    registerResponse,
                    UserResponse.class
            );


    // =====================================================
    // VERIFY REGISTRATION RESPONSE
    // =====================================================

    assertNotNull(registeredUser);

    assertNotNull(
            registeredUser.getId()
    );

    assertEquals(
            fullName,
            registeredUser.getFullName()
    );

    assertEquals(
            email,
            registeredUser.getEmail()
    );


    // =====================================================
    // STEP 2 — VERIFY DATABASE PERSISTENCE
    // =====================================================

    var persistedAfterRegistration =
            userRepository
                    .findByEmail(email)
                    .orElseThrow();


    assertNotNull(
            persistedAfterRegistration.getId()
    );

    assertEquals(
            fullName,
            persistedAfterRegistration.getFullName()
    );

    assertEquals(
            email,
            persistedAfterRegistration.getEmail()
    );

    assertEquals(
            "USER",
            persistedAfterRegistration.getRole()
    );


    // Password must NOT be stored as plain text.
    assertNotEquals(
            password,
            persistedAfterRegistration.getPassword()
    );


    // =====================================================
    // STEP 3 — LOGIN
    // =====================================================

    AuthRequest loginRequest =
            new AuthRequest();

    loginRequest.setEmail(email);
    loginRequest.setPassword(password);


    String loginResponse =
            mockMvc.perform(
                    post("/api/auth/login")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .content(
                                    objectMapper.writeValueAsString(
                                            loginRequest
                                    )
                            )
            )
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.token")
                            .isNotEmpty()
            )
            .andExpect(
                    jsonPath("$.tokenType")
                            .value("Bearer")
            )
            .andReturn()
            .getResponse()
            .getContentAsString();


    AuthResponse authResponse =
            objectMapper.readValue(
                    loginResponse,
                    AuthResponse.class
            );


    // =====================================================
    // VERIFY LOGIN RESPONSE
    // =====================================================

    assertNotNull(authResponse);

    assertNotNull(
            authResponse.getToken()
    );

    assertFalse(
            authResponse
                    .getToken()
                    .isBlank()
    );


    assertEquals(
            "Bearer",
            authResponse.getTokenType()
    );


    // =====================================================
    // STEP 4 — GET CURRENT USER
    //
    // Uses the JWT returned by login.
    // =====================================================

    mockMvc.perform(
            get("/api/users/me")
                    .header(
                            "Authorization",
                            "Bearer " +
                                    authResponse.getToken()
                    )
    )
            .andExpect(status().isOk())
            .andExpect(
                    jsonPath("$.id")
                            .value(
                                    registeredUser.getId()
                            )
            )
            .andExpect(
                    jsonPath("$.fullName")
                            .value(fullName)
            )
            .andExpect(
                    jsonPath("$.email")
                            .value(email)
            )
            .andExpect(
                    jsonPath("$.role")
                            .value("USER")
            );


    // =====================================================
    // STEP 5 — FINAL DATABASE VERIFICATION
    // =====================================================

    var finalUser =
            userRepository
                    .findByEmail(email)
                    .orElseThrow();


    assertEquals(
            registeredUser.getId(),
            finalUser.getId()
    );

    assertEquals(
            fullName,
            finalUser.getFullName()
    );

    assertEquals(
            email,
            finalUser.getEmail()
    );

    assertEquals(
            "USER",
            finalUser.getRole()
    );


    // Password remains encoded.
    assertNotEquals(
            password,
            finalUser.getPassword()
    );
}


}
