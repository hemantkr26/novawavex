
package com.novawavex.novawavex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.notification.NotificationPriority;
import com.novawavex.novawavex.notification.NotificationService;
import com.novawavex.novawavex.notification.NotificationType;
import com.novawavex.novawavex.notification.dto.NotificationResponse;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private String jwtToken;

    private Long userId;


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
                .andExpect(
                        status().isOk()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode loginJson =
                objectMapper.readTree(
                        loginResponse
                );


        assertNotNull(
                loginJson.get("token")
        );


        jwtToken =
                loginJson
                        .get("token")
                        .asText();


        assertFalse(
                jwtToken.isBlank()
        );


        /*
         * =====================================
         * GET CURRENT USER
         * =====================================
         */

        String profileResponse =
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


        JsonNode profileJson =
                objectMapper.readTree(
                        profileResponse
                );


        assertNotNull(
                profileJson.get("id")
        );


        userId =
                profileJson
                        .get("id")
                        .asLong();
    }


    /*
     * =========================================
     * T15.6.1
     *
     * CREATE NOTIFICATIONS
     * =========================================
     */

    @Test
    void createNotifications_shouldSucceed() {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        NotificationResponse first =
                notificationService.createNotification(
                        "T15.6 Test Notification 1",
                        "First notification integration test.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        user,
                        null
                );


        NotificationResponse second =
                notificationService.createNotification(
                        "T15.6 Test Notification 2",
                        "Second notification integration test.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.WARNING,
                        user,
                        null
                );


        assertNotNull(
                first
        );

        assertNotNull(
                first.getId()
        );

        assertEquals(
                "T15.6 Test Notification 1",
                first.getTitle()
        );

        assertFalse(
                first.isRead()
        );


        assertNotNull(
                second
        );

        assertNotNull(
                second.getId()
        );

        assertEquals(
                "T15.6 Test Notification 2",
                second.getTitle()
        );

        assertFalse(
                second.isRead()
        );
    }


    /*
     * =========================================
     * T15.6.2
     *
     * GET ALL NOTIFICATIONS
     * =========================================
     */

    @Test
    void getNotifications_shouldReturnUserNotifications()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        notificationService.createNotification(
                "T15.6 Get All",
                "Get all notifications test.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.INFO,
                user,
                null
        );


        String response =
                mockMvc.perform(
                        get("/api/notifications")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
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


        JsonNode json =
                objectMapper.readTree(
                        response
                );


        assertTrue(
                json.isArray()
        );

        assertFalse(
                json.isEmpty()
        );


        boolean found =
                false;

        for (JsonNode notification : json) {

            if ("T15.6 Get All".equals(
                    notification
                            .get("title")
                            .asText()
            )) {

                found = true;

                assertEquals(
                        "Get all notifications test.",
                        notification
                                .get("message")
                                .asText()
                );

                assertFalse(
                        notification
                                .get("read")
                                .asBoolean()
                );
            }
        }


        assertTrue(
                found,
                "Created test notification was not found"
        );
    }


    /*
     * =========================================
     * T15.6.3
     *
     * GET UNREAD NOTIFICATIONS
     * =========================================
     */

    @Test
    void getUnreadNotifications_shouldReturnUnreadNotifications()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        notificationService.createNotification(
                "T15.6 Unread",
                "Unread notification test.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.INFO,
                user,
                null
        );


        String response =
                mockMvc.perform(
                        get("/api/notifications/unread")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
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


        JsonNode json =
                objectMapper.readTree(
                        response
                );


        assertTrue(
                json.isArray()
        );


        boolean found =
                false;

        for (JsonNode notification : json) {

            assertFalse(
                    notification
                            .get("read")
                            .asBoolean()
            );


            if ("T15.6 Unread".equals(
                    notification
                            .get("title")
                            .asText()
            )) {

                found = true;
            }
        }


        assertTrue(
                found,
                "Unread test notification was not found"
        );
    }


    /*
     * =========================================
     * T15.6.4
     *
     * GET UNREAD COUNT
     * =========================================
     */

    @Test
    void getUnreadCount_shouldReturnUnreadCount()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        notificationService.createNotification(
                "T15.6 Count",
                "Unread count test.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.INFO,
                user,
                null
        );


        String response =
                mockMvc.perform(
                        get("/api/notifications/unread-count")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
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


        long count =
                Long.parseLong(
                        response
                );


        assertTrue(
                count >= 1
        );
    }


    /*
     * =========================================
     * T15.6.5
     *
     * MARK NOTIFICATION AS READ
     * =========================================
     */

    @Test
    void markNotificationAsRead_shouldSucceed()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        NotificationResponse created =
                notificationService.createNotification(
                        "T15.6 Mark Read",
                        "Mark read integration test.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        user,
                        null
                );


        String response =
                mockMvc.perform(
                        put(
                                "/api/notifications/"
                                        + created.getId()
                                        + "/read"
                        )
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


        JsonNode json =
                objectMapper.readTree(
                        response
                );


        assertEquals(
                created.getId().longValue(),
                json.get("id").asLong()
        );

        assertTrue(
                json.get("read").asBoolean()
        );
    }


    /*
     * =========================================
     * T15.6.6
     *
     * MARK ALL AS READ
     * =========================================
     */

    @Test
    void markAllAsRead_shouldSucceed()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        notificationService.createNotification(
                "T15.6 Mark All 1",
                "Mark all read test 1.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.INFO,
                user,
                null
        );


        notificationService.createNotification(
                "T15.6 Mark All 2",
                "Mark all read test 2.",
                NotificationType.WORKFLOW_STARTED,
                NotificationPriority.WARNING,
                user,
                null
        );


        mockMvc.perform(
                put("/api/notifications/read-all")
                        .param(
                                "userId",
                                userId.toString()
                        )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isNoContent()
        );


        String response =
                mockMvc.perform(
                        get("/api/notifications/unread-count")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
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


        assertEquals(
                0L,
                Long.parseLong(response)
        );
    }


    /*
     * =========================================
     * T15.6.7
     *
     * DELETE NOTIFICATION
     * =========================================
     */

    @Test
    void deleteNotification_shouldSucceed()
            throws Exception {

        User user =
                userRepository
                        .findByEmail(
                                "jwttest@novawavex.com"
                        )
                        .orElseThrow();


        NotificationResponse created =
                notificationService.createNotification(
                        "T15.6 Delete",
                        "Delete notification integration test.",
                        NotificationType.WORKFLOW_STARTED,
                        NotificationPriority.INFO,
                        user,
                        null
                );


        Long notificationId =
                created.getId();


        mockMvc.perform(
                delete(
                        "/api/notifications/"
                                + notificationId
                )
                        .header(
                                "Authorization",
                                "Bearer " + jwtToken
                        )
        )
        .andExpect(
                status().isNoContent()
        );


        String response =
                mockMvc.perform(
                        get("/api/notifications")
                                .param(
                                        "userId",
                                        userId.toString()
                                )
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


        JsonNode json =
                objectMapper.readTree(
                        response
                );


        for (JsonNode notification : json) {

            assertFalse(
                    notification
                            .get("id")
                            .asLong()
                            == notificationId
            );
        }
    }
}
