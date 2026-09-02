
package com.novawavex.novawavex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.dto.ProfileNameRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * =========================================
     * OBJECT MAPPER
     * =========================================
     *
     * NovaWavex does not expose ObjectMapper
     * as a Spring bean in this test context.
     *
     * Therefore, create it directly.
     */

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;


    /*
     * =========================================
     * LOGIN
     * =========================================
     */

    @BeforeEach
    void login() throws Exception {

        AuthRequest loginRequest =
                new AuthRequest();

        loginRequest.setEmail(
                "jwttest@novawavex.com"
        );

        loginRequest.setPassword(
                "Test12345"
        );

        String response =
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
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        assertNotNull(
                json.get("token")
        );

        jwtToken =
                json.get("token").asText();

        assertTrue(
                !jwtToken.isBlank()
        );
    }


    /*
     * =========================================
     * T15.4
     *
     * LOGIN
     *   ↓
     * GET PROFILE
     *   ↓
     * UPDATE NAME
     *   ↓
     * GET PROFILE AGAIN
     *
     * NOTE:
     *
     * Profile image is intentionally NOT tested
     * here because the current production API does
     * not expose:
     *
     * PUT /api/users/me/image
     *
     * No main/production files are changed by
     * this test.
     * =========================================
     */

    @Test
    void profileLifecycle_shouldSucceed()
            throws Exception {

        /*
         * =====================================
         * 1. GET CURRENT PROFILE
         * =====================================
         */

        String initialResponse =
                mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode initialProfile =
                objectMapper.readTree(
                        initialResponse
                );

        /*
         * =====================================
         * VERIFY INITIAL PROFILE
         * =====================================
         */

        assertNotNull(
                initialProfile.get("id")
        );

        assertEquals(
                "jwttest@novawavex.com",
                initialProfile
                        .get("email")
                        .asText()
        );

        assertNotNull(
                initialProfile.get("fullName")
        );

        assertNotNull(
                initialProfile.get("role")
        );


        /*
         * =====================================
         * 2. UPDATE PROFILE NAME
         * =====================================
         */

        ProfileNameRequest nameRequest =
                new ProfileNameRequest(
                        "T15.4 Profile Test User"
                );

        String nameResponse =
                mockMvc.perform(
                        put("/api/users/me/name")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                nameRequest
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode updatedNameProfile =
                objectMapper.readTree(
                        nameResponse
                );

        /*
         * =====================================
         * VERIFY UPDATED NAME
         * =====================================
         */

        assertEquals(
                "T15.4 Profile Test User",
                updatedNameProfile
                        .get("fullName")
                        .asText()
        );

        assertEquals(
                "jwttest@novawavex.com",
                updatedNameProfile
                        .get("email")
                        .asText()
        );

        assertNotNull(
                updatedNameProfile.get("id")
        );

        assertNotNull(
                updatedNameProfile.get("role")
        );


        /*
         * =====================================
         * 3. GET PROFILE AGAIN
         * =====================================
         */

        String finalResponse =
                mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + jwtToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode finalProfile =
                objectMapper.readTree(
                        finalResponse
                );


        /*
         * =====================================
         * VERIFY PROFILE ID
         * =====================================
         */

        assertEquals(
                initialProfile
                        .get("id")
                        .asLong(),

                finalProfile
                        .get("id")
                        .asLong()
        );


        /*
         * =====================================
         * VERIFY UPDATED NAME
         * =====================================
         */

        assertEquals(
                "T15.4 Profile Test User",
                finalProfile
                        .get("fullName")
                        .asText()
        );


        /*
         * =====================================
         * VERIFY EMAIL
         * =====================================
         */

        assertEquals(
                "jwttest@novawavex.com",
                finalProfile
                        .get("email")
                        .asText()
        );


        /*
         * =====================================
         * VERIFY ROLE
         * =====================================
         */

        assertNotNull(
                finalProfile.get("role")
        );


        /*
         * =====================================
         * VERIFY PROFILE IMAGE FIELD
         * =====================================
         *
         * The UserResponse currently contains the
         * profileImage field. We only verify that
         * the API response contains the field.
         *
         * We do NOT attempt to update it because the
         * production controller currently has no
         * /api/users/me/image endpoint.
         */

        assertNotNull(
                finalProfile.get("profileImage")
        );
    }
}
