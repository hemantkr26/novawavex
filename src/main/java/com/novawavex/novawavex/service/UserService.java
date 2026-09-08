
package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.AccountStatusUpdateRequest;
import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.RoleUpdateRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.entity.User;
import com.novawavex.novawavex.exception.DuplicateResourceException;
import com.novawavex.novawavex.exception.ResourceNotFoundException;
import com.novawavex.novawavex.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================
    // CREATE USER
    // =========================================

    public UserResponse createUser(UserRequest request) {

        if (userRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Email already registered"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

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
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
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
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
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
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
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
    // UPDATE USER ROLE
    // =========================================

    @Transactional
    public UserResponse updateUserRole(
            Long id,
            RoleUpdateRequest request) {

        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        String role =
                request.getRole()
                        .trim()
                        .toUpperCase();

        /*
         * NovaWavex currently supports:
         *
         * USER
         * ADMIN
         */

        if (!role.equals("USER")
                && !role.equals("ADMIN")) {

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

    @Transactional
    public UserResponse updateAccountStatus(
            Long id,
            AccountStatusUpdateRequest request) {

        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
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
    // USER RESPONSE MAPPER
    // =========================================

    private UserResponse toUserResponse(User user) {

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
