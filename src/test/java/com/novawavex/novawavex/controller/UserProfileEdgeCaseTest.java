
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class UserProfileEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * NovaWavex does not expose ObjectMapper
     * as a Spring bean in this test context.
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
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/auth/login")
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
     * T16.3.1
     * GET PROFILE - VALID REQUEST
     * =========================================
     */

    @Test
    void getProfile_withValidToken_shouldSucceed()
            throws Exception {

        String response =
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

        JsonNode profile =
                objectMapper.readTree(response);

        assertNotNull(
                profile.get("id")
        );

        assertEquals(
                "jwttest@novawavex.com",
                profile.get("email").asText()
        );

        assertNotNull(
                profile.get("fullName")
        );

        assertNotNull(
                profile.get("role")
        );
    }


    /*
     * =========================================
     * T16.3.2
     * GET PROFILE - PROFILE ID
     * =========================================
     */

    @Test
    void getProfile_shouldContainValidId()
            throws Exception {

        String response =
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

        JsonNode profile =
                objectMapper.readTree(response);

        assertNotNull(
                profile.get("id")
        );

        assertTrue(
                profile.get("id").asLong() > 0
        );
    }


    /*
     * =========================================
     * T16.3.3
     * GET PROFILE - EMAIL
     * =========================================
     */

    @Test
    void getProfile_shouldReturnAuthenticatedEmail()
            throws Exception {

        String response =
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

        JsonNode profile =
                objectMapper.readTree(response);

        assertEquals(
                "jwttest@novawavex.com",
                profile.get("email").asText()
        );
    }


    /*
     * =========================================
     * T16.3.4
     * UPDATE NAME - VALID NAME
     * =========================================
     */

    @Test
    void updateName_withValidName_shouldSucceed()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "T16.3 Edge Case User"
                );

        String response =
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
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode profile =
                objectMapper.readTree(response);

        assertEquals(
                "T16.3 Edge Case User",
                profile.get("fullName").asText()
        );

        assertEquals(
                "jwttest@novawavex.com",
                profile.get("email").asText()
        );
    }


    /*
     * =========================================
     * T16.3.5
     * UPDATE NAME - EMPTY NAME
     * =========================================
     */

    @Test
    void updateName_withEmptyName_shouldBeRejected()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest("");

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.3.6
     * UPDATE NAME - WHITESPACE NAME
     * =========================================
     */

    @Test
    void updateName_withWhitespaceName_shouldBeRejected()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest("   ");

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.3.7
     * UPDATE NAME - TOO SHORT
     * =========================================
     */

    @Test
    void updateName_withOneCharacter_shouldBeRejected()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest("A");

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.3.8
     * UPDATE NAME - TOO LONG
     * =========================================
     */

    @Test
    void updateName_withNameOver100Characters_shouldBeRejected()
            throws Exception {

        String longName =
                "A".repeat(101);

        ProfileNameRequest request =
                new ProfileNameRequest(
                        longName
                );

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.3.9
     * UPDATE NAME - NULL NAME
     * =========================================
     */

    @Test
    void updateName_withNullName_shouldBeRejected()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest();

        request.setFullName(null);

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isBadRequest()
        );
    }


    /*
     * =========================================
     * T16.3.10
     * UPDATE NAME - MINIMUM VALID LENGTH
     * =========================================
     */

    @Test
    void updateName_withTwoCharacterName_shouldSucceed()
            throws Exception {

        ProfileNameRequest request =
                new ProfileNameRequest(
                        "AB"
                );

        String response =
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
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode profile =
                objectMapper.readTree(response);

        assertEquals(
                "AB",
                profile.get("fullName").asText()
        );
    }


    /*
     * =========================================
     * T16.3.11
     * UPDATE NAME - 100 CHARACTER NAME
     * =========================================
     */

    @Test
    void updateName_withExactly100Characters_shouldSucceed()
            throws Exception {

        String validName =
                "A".repeat(100);

        ProfileNameRequest request =
                new ProfileNameRequest(
                        validName
                );

        String response =
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
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode profile =
                objectMapper.readTree(response);

        assertEquals(
                validName,
                profile.get("fullName").asText()
        );
    }


    /*
     * =========================================
     * T16.3.12
     * PROFILE PERSISTENCE AFTER NAME UPDATE
     * =========================================
     */

    @Test
    void updatedName_shouldPersistInProfile()
            throws Exception {

        String updatedName =
                "T16.3 Persistent Profile User";

        ProfileNameRequest request =
                new ProfileNameRequest(
                        updatedName
                );

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
                                        request
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );

        String response =
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

        JsonNode profile =
                objectMapper.readTree(response);

        assertEquals(
                updatedName,
                profile.get("fullName").asText()
        );

        assertEquals(
                "jwttest@novawavex.com",
                profile.get("email").asText()
        );

        assertNotNull(
                profile.get("id")
        );

        assertNotNull(
                profile.get("role")
        );
    }
}
