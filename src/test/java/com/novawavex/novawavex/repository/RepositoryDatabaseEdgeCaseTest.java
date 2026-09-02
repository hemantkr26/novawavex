
package com.novawavex.novawavex.repository;

import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.notification.Notification;
import com.novawavex.novawavex.notification.NotificationRepository;
import com.novawavex.novawavex.workflow.Workflow;
import com.novawavex.novawavex.workflow.WorkflowExecution;
import com.novawavex.novawavex.workflow.WorkflowExecutionRepository;
import com.novawavex.novawavex.workflow.WorkflowRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RepositoryDatabaseEdgeCaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    /*
     * =========================================
     * T16.8.1
     *
     * USER REPOSITORY
     * EXISTING USER LOOKUP
     * =========================================
     */

    @Test
    void userRepository_existingUser_shouldBeFound() {

        Optional<User> user =
                userRepository.findByEmail(
                        "jwttest@novawavex.com"
                );

        assertTrue(user.isPresent());

        assertEquals(
                "jwttest@novawavex.com",
                user.get().getEmail()
        );
    }


    /*
     * =========================================
     * T16.8.2
     *
     * USER REPOSITORY
     * NONEXISTENT USER
     * =========================================
     */

    @Test
    void userRepository_nonexistentEmail_shouldReturnEmpty() {

        Optional<User> user =
                userRepository.findByEmail(
                        "does-not-exist-t16-8@example.com"
                );

        assertTrue(user.isEmpty());
    }


    /*
     * =========================================
     * T16.8.3
     *
     * USER REPOSITORY
     * FIND BY ID
     * =========================================
     */

    @Test
    void userRepository_nonexistentId_shouldReturnEmpty() {

        Optional<User> user =
                userRepository.findById(
                        999999999L
                );

        assertTrue(user.isEmpty());
    }


    /*
     * =========================================
     * T16.8.4
     *
     * WORKFLOW REPOSITORY
     * USER OWNERSHIP LOOKUP
     * =========================================
     */

    @Test
    void workflowRepository_findByCreatedBy_shouldReturnOnlyUserWorkflows() {

        List<Workflow> workflows =
                workflowRepository.findByCreatedBy(
                        "jwttest@novawavex.com"
                );

        assertNotNull(workflows);

        workflows.forEach(workflow ->
                assertEquals(
                        "jwttest@novawavex.com",
                        workflow.getCreatedBy()
                )
        );
    }


    /*
     * =========================================
     * T16.8.5
     *
     * WORKFLOW REPOSITORY
     * NONEXISTENT WORKFLOW
     * =========================================
     */

    @Test
    void workflowRepository_nonexistentId_shouldReturnEmpty() {

        Optional<Workflow> workflow =
                workflowRepository.findById(
                        999999999L
                );

        assertTrue(workflow.isEmpty());
    }


    /*
     * =========================================
     * T16.8.6
     *
     * WORKFLOW REPOSITORY
     * OWNERSHIP-SAFE LOOKUP
     * =========================================
     */

    @Test
    void workflowRepository_nonexistentOwner_shouldReturnEmpty() {

        Optional<Workflow> workflow =
                workflowRepository.findByIdAndCreatedBy(
                        999999999L,
                        "nonexistent-owner-t16-8@example.com"
                );

        assertTrue(workflow.isEmpty());
    }


    /*
     * =========================================
     * T16.8.7
     *
     * WORKFLOW EXECUTION REPOSITORY
     * NONEXISTENT EXECUTION
     * =========================================
     */

    @Test
    void workflowExecutionRepository_nonexistentId_shouldReturnEmpty() {

        Optional<WorkflowExecution> execution =
                workflowExecutionRepository.findById(
                        999999999L
                );

        assertTrue(execution.isEmpty());
    }


    /*
     * =========================================
     * T16.8.8
     *
     * WORKFLOW EXECUTION REPOSITORY
     * NONEXISTENT WORKFLOW
     * =========================================
     */

    @Test
    void workflowExecutionRepository_nonexistentWorkflow_shouldReturnEmptyList() {

        List<WorkflowExecution> executions =
                workflowExecutionRepository.findByWorkflowId(
                        999999999L
                );

        assertNotNull(executions);

        assertTrue(executions.isEmpty());
    }


    /*
     * =========================================
     * T16.8.9
     *
     * WORKFLOW EXECUTION REPOSITORY
     * NONEXISTENT USER
     * =========================================
     */

    @Test
    void workflowExecutionRepository_nonexistentUser_shouldReturnEmptyList() {

        List<WorkflowExecution> executions =
                workflowExecutionRepository.findByCreatedBy(
                        "nonexistent-owner-t16-8@example.com"
                );

        assertNotNull(executions);

        assertTrue(executions.isEmpty());
    }


    /*
     * =========================================
     * T16.8.10
     *
     * NOTIFICATION REPOSITORY
     * NONEXISTENT USER
     * =========================================
     */

    @Test
    void notificationRepository_nonexistentUser_shouldReturnEmptyList() {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                999999999L
                        );

        assertNotNull(notifications);

        assertTrue(notifications.isEmpty());
    }


    /*
     * =========================================
     * T16.8.11
     *
     * NOTIFICATION REPOSITORY
     * UNREAD NONEXISTENT USER
     * =========================================
     */

    @Test
    void notificationRepository_nonexistentUserUnread_shouldReturnEmptyList() {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                999999999L
                        );

        assertNotNull(notifications);

        assertTrue(notifications.isEmpty());
    }


    /*
     * =========================================
     * T16.8.12
     *
     * NOTIFICATION REPOSITORY
     * UNREAD COUNT FOR NONEXISTENT USER
     * =========================================
     */

    @Test
    void notificationRepository_nonexistentUserUnreadCount_shouldBeZero() {

        long unreadCount =
                notificationRepository
                        .countByUserIdAndReadFalse(
                                999999999L
                        );

        assertEquals(
                0L,
                unreadCount
        );
    }


    /*
     * =========================================
     * T16.8.13
     *
     * NOTIFICATION REPOSITORY
     * NONEXISTENT WORKFLOW
     * =========================================
     */

    @Test
    void notificationRepository_nonexistentWorkflow_shouldReturnEmptyList() {

        List<Notification> notifications =
                notificationRepository.findByWorkflowId(
                        999999999L
                );

        assertNotNull(notifications);

        assertTrue(notifications.isEmpty());
    }


    /*
     * =========================================
     * T16.8.14
     *
     * DATABASE PERSISTENCE
     * USER SAVE AND RETRIEVE
     * =========================================
     */

    @Test
    void userRepository_saveAndRetrieve_shouldPersistUser() {

        User user = new User();

        user.setFullName(
                "T16.8 Repository Test User"
        );

        user.setEmail(
                "t168-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPassword(
                "Test12345"
        );

        /*
         * Role is NOT NULL in the database.
         * Set the required test value.
         */
        user.setRole("USER");

        User savedUser =
                userRepository.save(user);

        assertNotNull(
                savedUser.getId()
        );

        Optional<User> retrievedUser =
                userRepository.findById(
                        savedUser.getId()
                );

        assertTrue(
                retrievedUser.isPresent()
        );

        assertEquals(
                savedUser.getEmail(),
                retrievedUser.get().getEmail()
        );

        assertEquals(
                savedUser.getFullName(),
                retrievedUser.get().getFullName()
        );

        assertEquals(
                "USER",
                retrievedUser.get().getRole()
        );
    }


    /*
     * =========================================
     * T16.8.15
     *
     * DATABASE PERSISTENCE
     * DELETE AND VERIFY
     * =========================================
     */

    @Test
    void userRepository_deleteAndRetrieve_shouldReturnEmpty() {

        User user = new User();

        user.setFullName(
                "T16.8 Delete Test User"
        );

        user.setEmail(
                "t168-delete-" + System.nanoTime()
                        + "@example.com"
        );

        user.setPassword(
                "Test12345"
        );

        /*
         * Role is NOT NULL in the database.
         * Set the required test value.
         */
        user.setRole("USER");

        User savedUser =
                userRepository.save(user);

        Long userId =
                savedUser.getId();

        assertNotNull(userId);

        userRepository.deleteById(
                userId
        );

        Optional<User> deletedUser =
                userRepository.findById(
                        userId
                );

        assertTrue(
                deletedUser.isEmpty()
        );
    }
}
