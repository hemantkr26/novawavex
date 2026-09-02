
package com.novawavex.novawavex.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.workflow.WorkflowStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DtoValidationEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * =========================================
     * OBJECT MAPPER
     * =========================================
     *
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

        String loginJson =
                """
                {
                    "email": "jwttest@novawavex.com",
                    "password": "Test12345"
                }
                """;

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginJson)
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
     * T16.7.1
     *
     * AUTH REQUEST
     * BLANK EMAIL
     * =========================================
     */

    @Test
    void authRequest_withBlankEmail_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "email": "",
                    "password": "Test12345"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.2
     *
     * AUTH REQUEST
     * INVALID EMAIL
     * =========================================
     */

    @Test
    void authRequest_withInvalidEmail_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "email": "invalid-email",
                    "password": "Test12345"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.3
     *
     * AUTH REQUEST
     * SHORT PASSWORD
     * =========================================
     */

    @Test
    void authRequest_withShortPassword_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "email": "jwttest@novawavex.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.4
     *
     * FORGOT PASSWORD
     * BLANK EMAIL
     * =========================================
     */

    @Test
    void forgotPassword_withBlankEmail_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "email": ""
                }
                """;

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.5
     *
     * FORGOT PASSWORD
     * INVALID EMAIL
     * =========================================
     */

    @Test
    void forgotPassword_withInvalidEmail_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "email": "not-an-email"
                }
                """;

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.6
     *
     * RESET PASSWORD
     * BLANK RESET TOKEN
     * =========================================
     */

    @Test
    void resetPassword_withBlankToken_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "resetToken": "",
                    "newPassword": "NewPassword123",
                    "confirmPassword": "NewPassword123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.7
     *
     * RESET PASSWORD
     * SHORT NEW PASSWORD
     * =========================================
     */

    @Test
    void resetPassword_withShortNewPassword_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "resetToken": "invalid-test-token",
                    "newPassword": "123",
                    "confirmPassword": "123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.8
     *
     * PROFILE NAME
     * BLANK NAME
     * =========================================
     */

    @Test
    void profileName_withBlankName_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "fullName": ""
                }
                """;

        mockMvc.perform(
                put("/api/users/me/name")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.9
     *
     * PROFILE NAME
     * TOO SHORT
     * =========================================
     */

    @Test
    void profileName_withTooShortName_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "fullName": "A"
                }
                """;

        mockMvc.perform(
                put("/api/users/me/name")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.10
     *
     * WORKFLOW REQUEST
     * BLANK NAME
     * =========================================
     */

    @Test
    void workflowRequest_withBlankName_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "name": "",
                    "description": "DTO validation test",
                    "status": "DRAFT"
                }
                """;

        mockMvc.perform(
                post("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.11
     *
     * WORKFLOW REQUEST
     * MISSING STATUS
     * =========================================
     */

    @Test
    void workflowRequest_withMissingStatus_shouldBeRejected()
            throws Exception {

        String json =
                """
                {
                    "name": "DTO Validation Workflow",
                    "description": "DTO validation test"
                }
                """;

        mockMvc.perform(
                post("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================
     * T16.7.12
     *
     * WORKFLOW REQUEST
     * OVERSIZED DESCRIPTION
     * =========================================
     */

    @Test
    void workflowRequest_withOversizedDescription_shouldBeRejected()
            throws Exception {

        String oversizedDescription =
                "A".repeat(1001);

        String json =
                objectMapper.createObjectNode()
                        .put(
                                "name",
                                "DTO Validation Workflow"
                        )
                        .put(
                                "description",
                                oversizedDescription
                        )
                        .put(
                                "status",
                                WorkflowStatus.DRAFT.name()
                        )
                        .toString();

        mockMvc.perform(
                post("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        )
        .andExpect(
                status().isBadRequest()
        );
    }
}
