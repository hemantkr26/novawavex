
package com.novawavex.novawavex.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    /*
     * =========================================
     * T15.5.1
     *
     * UNAUTHENTICATED REQUEST
     * =========================================
     *
     * A protected endpoint must reject a request
     * when no JWT is supplied.
     */

    @Test
    void protectedEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T15.5.2
     *
     * INVALID JWT
     * =========================================
     *
     * A malformed/invalid JWT must not grant access
     * to protected resources.
     */

    @Test
    void protectedEndpoint_withInvalidToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T15.5.3
     *
     * EMPTY BEARER TOKEN
     * =========================================
     *
     * "Bearer " without an actual token must not
     * authenticate the request.
     */

    @Test
    void protectedEndpoint_withEmptyBearerToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/users/me")
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
     * =========================================
     * T15.5.4
     *
     * MISSING AUTHORIZATION HEADER
     * =========================================
     *
     * Explicitly verify that another protected
     * endpoint cannot be accessed without JWT.
     */

    @Test
    void workflowsEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T15.5.5
     *
     * INVALID JWT ON WORKFLOW ENDPOINT
     * =========================================
     */

    @Test
    void workflowsEndpoint_withInvalidToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                get("/api/workflows")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }


    /*
     * =========================================
     * T15.5.6
     *
     * PROTECTED WRITE ENDPOINT
     * =========================================
     *
     * Verify that a protected POST endpoint also
     * rejects unauthenticated access.
     *
     * The request is intentionally invalid/empty;
     * security must reject it before controller
     * validation is reached.
     */

    @Test
    void workflowCreateEndpoint_withoutToken_shouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(
                post("/api/workflows")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }
}
