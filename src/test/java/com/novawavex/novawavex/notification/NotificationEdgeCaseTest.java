package com.novawavex.novawavex.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novawavex.novawavex.dto.AuthRequest;
import com.novawavex.novawavex.entity.User;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationEdgeCaseTest {


@Autowired
private MockMvc mockMvc;

@Autowired
private NotificationService notificationService;

@Autowired
private UserRepository userRepository;

/*
 * NovaWavex does not expose ObjectMapper
 * as a Spring bean in this test context.
 */
private final ObjectMapper objectMapper =
        new ObjectMapper();

private String jwtToken;

private User testUser;

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

    /*
     * =====================================
     * LOAD TEST USER
     * =====================================
     */

    testUser =
            userRepository
                    .findByEmail(
                            "jwttest@novawavex.com"
                    )
                    .orElseThrow();

    userId =
            testUser.getId();

    assertNotNull(
            userId
    );
}


/*
 * =========================================
 * T16.6.1
 *
 * GET ALL NOTIFICATIONS
 * =========================================
 */

@Test
void getNotifications_shouldReturnSuccess()
        throws Exception {

    notificationService.createNotification(
            "T16.6 Notification",
            "Notification edge-case test.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
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

    JsonNode notifications =
            objectMapper.readTree(response);

    assertTrue(
            notifications.isArray()
    );
}


/*
 * =========================================
 * T16.6.2
 *
 * GET UNREAD NOTIFICATIONS
 * =========================================
 */

@Test
void getUnreadNotifications_shouldReturnSuccess()
        throws Exception {

    notificationService.createNotification(
            "T16.6 Unread",
            "Unread notification.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
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

    JsonNode notifications =
            objectMapper.readTree(response);

    assertTrue(
            notifications.isArray()
    );
}


/*
 * =========================================
 * T16.6.3
 *
 * GET UNREAD COUNT
 * =========================================
 */

@Test
void getUnreadCount_shouldReturnSuccess()
        throws Exception {

    notificationService.createNotification(
            "T16.6 Count",
            "Unread count test.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
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

    JsonNode count =
            objectMapper.readTree(response);

    assertTrue(
            count.isNumber()
    );

    assertTrue(
            count.asLong() >= 1
    );
}


/*
 * =========================================
 * T16.6.4
 *
 * MARK NOTIFICATION AS READ
 * =========================================
 */

@Test
void markAsRead_shouldReturnSuccess()
        throws Exception {

    NotificationResponse notification =
            notificationService.createNotification(
                    "T16.6 Mark Read",
                    "Mark as read test.",
                    NotificationType.SYSTEM_ALERT,
                    NotificationPriority.INFO,
                    testUser,
                    null
            );

    assertNotNull(
            notification
    );

    assertNotNull(
            notification.getId()
    );

    String response =
            mockMvc.perform(
                    put(
                            "/api/notifications/"
                                    + notification.getId()
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

    JsonNode result =
            objectMapper.readTree(response);

    assertEquals(
            notification.getId().longValue(),
            result.get("id").asLong()
    );
}


/*
 * =========================================
 * T16.6.5
 *
 * MARK ALL AS READ
 * =========================================
 */

@Test
void markAllAsRead_shouldReturnNoContent()
        throws Exception {

    notificationService.createNotification(
            "T16.6 Read All 1",
            "First notification.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
            null
    );

    notificationService.createNotification(
            "T16.6 Read All 2",
            "Second notification.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
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
}


/*
 * =========================================
 * T16.6.6
 *
 * DELETE NOTIFICATION
 * =========================================
 */

@Test
void deleteNotification_shouldReturnNoContent()
        throws Exception {

    NotificationResponse notification =
            notificationService.createNotification(
                    "T16.6 Delete",
                    "Delete notification test.",
                    NotificationType.SYSTEM_ALERT,
                    NotificationPriority.INFO,
                    testUser,
                    null
            );

    assertNotNull(
            notification.getId()
    );

    mockMvc.perform(
            delete(
                    "/api/notifications/"
                            + notification.getId()
            )
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isNoContent()
    );
}


/*
 * =========================================
 * T16.6.7
 *
 * GET NOTIFICATIONS WITHOUT USER ID
 *
 * Current exception handling returns 500
 * for the missing required request parameter.
 * =========================================
 */

@Test
void getNotifications_withoutUserId_shouldBeRejected()
        throws Exception {

    mockMvc.perform(
            get("/api/notifications")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isInternalServerError()
    );
}


/*
 * =========================================
 * T16.6.8
 *
 * GET UNREAD WITHOUT USER ID
 *
 * Current exception handling returns 500.
 * =========================================
 */

@Test
void getUnreadNotifications_withoutUserId_shouldBeRejected()
        throws Exception {

    mockMvc.perform(
            get("/api/notifications/unread")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isInternalServerError()
    );
}


/*
 * =========================================
 * T16.6.9
 *
 * GET UNREAD COUNT WITHOUT USER ID
 *
 * Current exception handling returns 500.
 * =========================================
 */

@Test
void getUnreadCount_withoutUserId_shouldBeRejected()
        throws Exception {

    mockMvc.perform(
            get("/api/notifications/unread-count")
                    .header(
                            "Authorization",
                            "Bearer " + jwtToken
                    )
    )
    .andExpect(
            status().isInternalServerError()
    );
}


/*
 * =========================================
 * T16.6.10
 *
 * MARK NONEXISTENT NOTIFICATION
 *
 * NotificationService throws
 * IllegalArgumentException.
 *
 * We verify that it does not return
 * a successful HTTP response.
 * =========================================
 */

@Test
void markAsRead_withNonexistentId_shouldNotReturnSuccess()
        throws Exception {

    int statusCode =
            mockMvc.perform(
                    put(
                            "/api/notifications/999999999/read"
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + jwtToken
                            )
            )
            .andReturn()
            .getResponse()
            .getStatus();

    assertTrue(
            statusCode >= 400
    );
}


/*
 * =========================================
 * T16.6.11
 *
 * DELETE NONEXISTENT NOTIFICATION
 * =========================================
 */

@Test
void deleteNotification_withNonexistentId_shouldNotReturnSuccess()
        throws Exception {

    int statusCode =
            mockMvc.perform(
                    delete(
                            "/api/notifications/999999999"
                    )
                            .header(
                                    "Authorization",
                                    "Bearer " + jwtToken
                            )
            )
            .andReturn()
            .getResponse()
            .getStatus();

    assertTrue(
            statusCode >= 400
    );
}


/*
 * =========================================
 * T16.6.12
 *
 * UNREAD COUNT AFTER MARK ALL READ
 * =========================================
 */

@Test
void unreadCount_afterMarkAllAsRead_shouldReturnZero()
        throws Exception {

    notificationService.createNotification(
            "T16.6 Final",
            "Final unread notification.",
            NotificationType.SYSTEM_ALERT,
            NotificationPriority.INFO,
            testUser,
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

    JsonNode count =
            objectMapper.readTree(response);

    assertEquals(
            0,
            count.asLong()
    );
}


}

