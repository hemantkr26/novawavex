package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.AccountStatusUpdateRequest;
import com.novawavex.novawavex.dto.ProfileImageRequest;
import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.RoleUpdateRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;

import com.novawavex.novawavex.entity.User;

import com.novawavex.novawavex.exception.DuplicateResourceException;
import com.novawavex.novawavex.exception.ResourceNotFoundException;

import com.novawavex.novawavex.repository.PasswordResetTokenRepository;
import com.novawavex.novawavex.repository.UserRepository;

import com.novawavex.novawavex.workflow.Workflow;
import com.novawavex.novawavex.workflow.WorkflowRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final WorkflowRepository workflowRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            WorkflowRepository workflowRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;

        this.workflowRepository = workflowRepository;

        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // CREATE USER
    // =========================================

    public UserResponse createUser(UserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {

            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                encodedPassword,
                "USER"
        );

        User savedUser =
                userRepository.save(user);

        return toUserResponse(savedUser);
    }

    // =========================================
    // GET ALL USERS
    // =========================================

    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    // =========================================
    // GET USER BY ID
    // =========================================

    public UserResponse getUserById(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return toUserResponse(user);
    }

    // =========================================
    // GET CURRENT AUTHENTICATED USER
    // =========================================

    public UserResponse getCurrentUser(String email) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return toUserResponse(user);
    }

    // =========================================
    // UPDATE CURRENT USER NAME
    // =========================================

    @Transactional
    public UserResponse updateCurrentUserName(
            String email,
            ProfileNameRequest request) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String fullName =
                request.getFullName().trim();

        if (fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "Full name cannot be empty"
            );
        }

        user.setFullName(fullName);

        User updatedUser =
                userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    // =========================================
    // UPDATE CURRENT USER PROFILE IMAGE
    // =========================================
    //
    // Authenticated users can update their
    // own profile image.
    //
    // The frontend sends:
    //
    // {
    //     "profileImage": "data:image/..."
    // }
    //
    // =========================================

    @Transactional
    public UserResponse updateCurrentUserProfileImage(
            String email,
            ProfileImageRequest request) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String profileImage =
                request.getProfileImage();

        if (profileImage == null ||
                profileImage.trim().isBlank()) {

            throw new IllegalArgumentException(
                    "Profile image cannot be empty"
            );
        }

        user.setProfileImage(
                profileImage.trim()
        );

        User updatedUser =
                userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    // =========================================
    // UPDATE USER ROLE
    // =========================================
    //
    // ADMIN ONLY
    //
    // USER <-> ADMIN
    //

    @Transactional
    public UserResponse updateUserRole(
            Long id,
            RoleUpdateRequest request) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String role =
                request.getRole()
                        .trim()
                        .toUpperCase();

        if (!role.equals("USER") &&
            !role.equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Role must be USER or ADMIN"
            );
        }

        user.setRole(role);

        User updatedUser =
                userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    // =========================================
    // UPDATE ACCOUNT STATUS
    // =========================================
    //
    // ADMIN ONLY
    //

    @Transactional
    public UserResponse updateAccountStatus(
            Long id,
            AccountStatusUpdateRequest request) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        user.setEnabled(
                request.isEnabled()
        );

        User updatedUser =
                userRepository.save(user);

        return toUserResponse(updatedUser);
    }

    // =========================================
    // DELETE CURRENT AUTHENTICATED USER
    // =========================================

    @Transactional
    public void deleteCurrentUser(String email) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        /*
         * Do not allow the application to lose
         * its final ADMIN account.
         */

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            long adminCount =
                    userRepository.countByRole("ADMIN");

            if (adminCount <= 1) {

                throw new IllegalArgumentException(
                        "The last ADMIN account cannot be deleted"
                );
            }
        }

        deleteUserAndOwnedWorkflows(user);
    }

    // =========================================
    // DELETE USER BY ID
    // ADMIN ONLY
    // =========================================

    @Transactional
    public void deleteUser(
            Long id,
            String requesterEmail) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        /*
         * An ADMIN must use the normal
         * Profile → Delete Account flow
         * to delete their own account.
         *
         * This prevents accidental self-deletion
         * from the admin user-management endpoint.
         */

        if (user.getEmail().equalsIgnoreCase(requesterEmail)) {

            throw new IllegalArgumentException(
                    "Use your Profile page to delete your own account"
            );
        }

        /*
         * Never allow the final ADMIN account
         * to be deleted.
         */

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            long adminCount =
                    userRepository.countByRole("ADMIN");

            if (adminCount <= 1) {

                throw new IllegalArgumentException(
                        "The last ADMIN account cannot be deleted"
                );
            }
        }

        deleteUserAndOwnedWorkflows(user);
    }

    // =========================================
    // DELETE USER + OWNED WORKFLOWS
    // =========================================
    //
    // Workflows store ownership using:
    //
    // createdBy = user email
    //
    // Therefore workflows are removed first.
    //
    // Password reset tokens also reference
    // the user through user_id.
    //
    // Therefore password reset tokens are
    // removed before deleting the user.
    //

    private void deleteUserAndOwnedWorkflows(
            User user) {

        List<Workflow> workflows =
                workflowRepository.findByCreatedBy(
                        user.getEmail()
                );

        if (!workflows.isEmpty()) {

            workflowRepository.deleteAll(
                    workflows
            );
        }

        /*
         * Password reset tokens contain a foreign-key
         * reference to users.id.
         *
         * Delete all tokens first so PostgreSQL
         * allows the user deletion.
         */

        passwordResetTokenRepository.deleteByUser(user);

        /*
         * Finally delete the user.
         */

        userRepository.delete(user);
    }

    // =========================================
    // USER RESPONSE MAPPER
    // =========================================

    private UserResponse toUserResponse(
            User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImage(),
                user.isEnabled()
        );
    }
}