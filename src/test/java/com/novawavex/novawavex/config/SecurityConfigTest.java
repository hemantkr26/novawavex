
package com.novawavex.novawavex.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.novawavex.novawavex.service.JwtService;
import com.novawavex.novawavex.workflow.WorkflowService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private WorkflowService workflowService;

    private String userToken;

    private String adminToken;

    private final String USER_EMAIL =
            "security-user@novawavex.com";

    private final String ADMIN_EMAIL =
            "security-admin@novawavex.com";

    @BeforeEach
    void setUp() {

        userToken =
                jwtService.generateToken(
                        1L,
                        USER_EMAIL,
                        "Security User",
                        "USER"
                );

        adminToken =
                jwtService.generateToken(
                        2L,
                        ADMIN_EMAIL,
                        "Security Admin",
                        "ADMIN"
                );
    }

    /*
     * =========================================================
     * SECURITY FILTER CHAIN
     * =========================================================
     */

    @Test
    void securityFilterChain_shouldLoad() {

        assertNotNull(securityFilterChain);
    }

    /*
     * =========================================================
     * PUBLIC AUTHENTICATION ENDPOINTS
     * =========================================================
     */

    @Test
    void loginEndpoint_shouldBePublic()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                result ->
                        assertNotUnauthorizedOrForbidden(
                                result.getResponse().getStatus()
                        )
        );
    }

    @Test
    void registerEndpoint_shouldBePublic()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                result ->
                        assertNotUnauthorizedOrForbidden(
                                result.getResponse().getStatus()
                        )
        );
    }

    @Test
    void forgotPasswordEndpoint_shouldBePublic()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                result ->
                        assertNotUnauthorizedOrForbidden(
                                result.getResponse().getStatus()
                        )
        );
    }

    @Test
    void resetPasswordEndpoint_shouldBePublic()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                result ->
                        assertNotUnauthorizedOrForbidden(
                                result.getResponse().getStatus()
                        )
        );
    }

    /*
     * =========================================================
     * ACTUATOR
     * =========================================================
     */

    @Test
    void actuatorHealth_shouldBePublic()
            throws Exception {

        mockMvc.perform(
                get("/actuator/health")
        )
        .andExpect(
                status().isOk()
        );
    }

    /*
     * =========================================================
     * PROTECTED WORKFLOW ENDPOINT
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withoutJwt_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
        )
        .andExpect(
                status().isUnauthorized()
        )
        .andExpect(
                content().contentTypeCompatibleWith(
                        "application/json"
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "\"status\": 401"
                        )
                )
        );
    }

    /*
     * =========================================================
     * INVALID JWT
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withInvalidJwt_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer invalid-token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        )
        .andExpect(
                content().contentTypeCompatibleWith(
                        "application/json"
                )
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "\"status\":401"
                        )
                )
        );
    }

    /*
     * =========================================================
     * EMPTY JWT
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withEmptyBearerToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer "
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    /*
     * =========================================================
     * INVALID AUTHORIZATION SCHEME
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withInvalidScheme_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Basic invalid-token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    /*
     * =========================================================
     * VALID USER JWT
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withUserJwt_shouldBeAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(
                result ->
                        assertNotUnauthorized(
                                result.getResponse().getStatus()
                        )
        );
    }

    /*
     * =========================================================
     * VALID ADMIN JWT
     * =========================================================
     */

    @Test
    void protectedWorkflowEndpoint_withAdminJwt_shouldBeAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
        .andExpect(
                result ->
                        assertNotUnauthorized(
                                result.getResponse().getStatus()
                        )
        );
    }

    /*
     * =========================================================
     * CURRENT USER PROFILE
     * =========================================================
     */

    @Test
    void currentUserProfile_withoutJwt_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void currentUserProfile_withUserJwt_shouldBeAuthenticated()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(
                result ->
                        assertNotUnauthorized(
                                result.getResponse().getStatus()
                        )
        );
    }

    /*
     * =========================================================
     * ADMIN AUTHORIZATION
     * =========================================================
     */

    @Test
    void adminUserEndpoint_withUserJwt_shouldReturn403()
            throws Exception {

        mockMvc.perform(
                get("/api/users/999999")
                        .header(
                                "Authorization",
                                "Bearer " + userToken
                        )
        )
        .andExpect(
                status().isForbidden()
        )
        .andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString(
                                "\"status\": 403"
                        )
                )
        );
    }

    @Test
    void adminUserEndpoint_withAdminJwt_shouldNotReturn403()
            throws Exception {

        mockMvc.perform(
                get("/api/users/999999")
                        .header(
                                "Authorization",
                                "Bearer " + adminToken
                        )
        )
        .andExpect(
                result ->
                        assertNotForbidden(
                                result.getResponse().getStatus()
                        )
        );
    }

    /*
     * =========================================================
     * CHANGE PASSWORD
     * =========================================================
     *
     * Current SecurityConfig permits /api/auth/**.
     *
     * Therefore this request reaches the controller.
     * An empty JSON object fails validation and returns 400.
     *
     * This test verifies the CURRENT behavior.
     *
     * We will separately review whether change-password
     * should require JWT authentication.
     */

    @Test
    void changePassword_withoutJwt_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                post("/api/auth/change-password")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().isBadRequest()
        );
    }

    /*
     * =========================================================
     * ASSERTION HELPERS
     * =========================================================
     */

    private void assertNotUnauthorizedOrForbidden(
            int status) {

        if (status == 401 || status == 403) {

            throw new AssertionError(
                    "Expected public endpoint but received HTTP "
                            + status
            );
        }
    }

    private void assertNotUnauthorized(
            int status) {

        if (status == 401) {

            throw new AssertionError(
                    "Expected authenticated request but received HTTP 401"
            );
        }
    }

    private void assertNotForbidden(
            int status) {

        if (status == 403) {

            throw new AssertionError(
                    "Expected ADMIN authorization but received HTTP 403"
            );
        }
    }
}
