
package com.novawavex.novawavex.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.service.JwtService;
import com.novawavex.novawavex.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    /*
     * Required because the application's JWT filter depends
     * on JwtService.
     */
    @MockitoBean
    private JwtService jwtService;

    /*
     * Test-only configuration.
     *
     * Spring Boot 4's MVC test slice does not expose the
     * application's ObjectMapper in this test context, so
     * we provide one explicitly for request serialization.
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    /*
     * =========================================
     * CREATE USER
     * =========================================
     */

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {

        UserRequest request = new UserRequest();

        request.setFullName("Hemant Kumar");
        request.setEmail("hemant@novawavex.com");
        request.setPassword("Test12345");

        UserResponse response = new UserResponse(
                1L,
                "Hemant Kumar",
                "hemant@novawavex.com",
                "USER",
                null
        );

        when(userService.createUser(any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.fullName")
                .value("Hemant Kumar"))
        .andExpect(jsonPath("$.email")
                .value("hemant@novawavex.com"))
        .andExpect(jsonPath("$.role")
                .value("USER"));

        verify(userService)
                .createUser(any(UserRequest.class));
    }

    /*
     * =========================================
     * CREATE USER - INVALID REQUEST
     * =========================================
     */

    @Test
    void createUser_withInvalidRequest_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = new UserRequest();

        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPassword("short");

        mockMvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());
    }

    /*
     * =========================================
     * GET ALL USERS
     * =========================================
     */

    @Test
    void getAllUsers_shouldReturnUsers() throws Exception {

        UserResponse user1 = new UserResponse(
                1L,
                "Hemant Kumar",
                "hemant@novawavex.com",
                "USER",
                null
        );

        UserResponse user2 = new UserResponse(
                2L,
                "Test User",
                "test@novawavex.com",
                "USER",
                null
        );

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(
                get("/api/users")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].fullName")
                .value("Hemant Kumar"))
        .andExpect(jsonPath("$[0].email")
                .value("hemant@novawavex.com"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].fullName")
                .value("Test User"));

        verify(userService).getAllUsers();
    }

    /*
     * =========================================
     * GET ALL USERS - EMPTY
     * =========================================
     */

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList()
            throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/users")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    /*
     * =========================================
     * GET CURRENT USER
     * =========================================
     */

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("hemant@novawavex.com");

        UserResponse response = new UserResponse(
                1L,
                "Hemant Kumar",
                "hemant@novawavex.com",
                "USER",
                null
        );

        when(userService.getCurrentUser(
                "hemant@novawavex.com"))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/users/me")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.fullName")
                .value("Hemant Kumar"))
        .andExpect(jsonPath("$.email")
                .value("hemant@novawavex.com"))
        .andExpect(jsonPath("$.role")
                .value("USER"));

        verify(userService)
                .getCurrentUser("hemant@novawavex.com");
    }

    /*
     * =========================================
     * GET CURRENT USER - AUTHENTICATION EMAIL
     * =========================================
     */

    @Test
    void getCurrentUser_shouldUseAuthenticationEmail()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("another@novawavex.com");

        UserResponse response = new UserResponse(
                5L,
                "Another User",
                "another@novawavex.com",
                "USER",
                null
        );

        when(userService.getCurrentUser(
                "another@novawavex.com"))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/users/me")
                        .principal(authentication)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email")
                .value("another@novawavex.com"));

        verify(userService)
                .getCurrentUser("another@novawavex.com");
    }

    /*
     * =========================================
     * UPDATE CURRENT USER NAME
     * =========================================
     */

    @Test
    void updateCurrentUserName_shouldReturnUpdatedUser()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("hemant@novawavex.com");

        ProfileNameRequest request =
                new ProfileNameRequest();

        request.setFullName("Hemant Kumar Updated");

        UserResponse response = new UserResponse(
                1L,
                "Hemant Kumar Updated",
                "hemant@novawavex.com",
                "USER",
                null
        );

        when(userService.updateCurrentUserName(
                eq("hemant@novawavex.com"),
                any(ProfileNameRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/users/me/name")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.fullName")
                .value("Hemant Kumar Updated"))
        .andExpect(jsonPath("$.email")
                .value("hemant@novawavex.com"));

        verify(userService).updateCurrentUserName(
                eq("hemant@novawavex.com"),
                any(ProfileNameRequest.class)
        );
    }

    /*
     * =========================================
     * UPDATE CURRENT USER NAME
     * - AUTHENTICATION EMAIL
     * =========================================
     */

    @Test
    void updateCurrentUserName_shouldUseAuthenticationEmail()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@novawavex.com");

        ProfileNameRequest request =
                new ProfileNameRequest();

        request.setFullName("Updated Name");

        UserResponse response = new UserResponse(
                10L,
                "Updated Name",
                "user@novawavex.com",
                "USER",
                null
        );

        when(userService.updateCurrentUserName(
                eq("user@novawavex.com"),
                any(ProfileNameRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/users/me/name")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email")
                .value("user@novawavex.com"))
        .andExpect(jsonPath("$.fullName")
                .value("Updated Name"));

        verify(userService).updateCurrentUserName(
                eq("user@novawavex.com"),
                any(ProfileNameRequest.class)
        );
    }

    /*
     * =========================================
     * UPDATE CURRENT USER NAME
     * - INVALID REQUEST
     * =========================================
     */

    @Test
    void updateCurrentUserName_withInvalidRequest_shouldReturnBadRequest()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("hemant@novawavex.com");

        ProfileNameRequest request =
                new ProfileNameRequest();

        request.setFullName("");

        mockMvc.perform(
                put("/api/users/me/name")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());
    }

    /*
     * =========================================
     * GET USER BY ID
     * =========================================
     */

    @Test
    void getUserById_shouldReturnUser() throws Exception {

        UserResponse response = new UserResponse(
                25L,
                "Hemant Kumar",
                "hemant@novawavex.com",
                "USER",
                null
        );

        when(userService.getUserById(25L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/users/25")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(25))
        .andExpect(jsonPath("$.fullName")
                .value("Hemant Kumar"))
        .andExpect(jsonPath("$.email")
                .value("hemant@novawavex.com"))
        .andExpect(jsonPath("$.role")
                .value("USER"));

        verify(userService)
                .getUserById(25L);
    }

    /*
     * =========================================
     * GET USER BY ID
     * - VERIFY PROVIDED ID
     * =========================================
     */

    @Test
    void getUserById_shouldUseProvidedId()
            throws Exception {

        UserResponse response = new UserResponse(
                99L,
                "Test User",
                "test@novawavex.com",
                "USER",
                null
        );

        when(userService.getUserById(99L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/users/99")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(99));

        verify(userService)
                .getUserById(99L);
    }
}
