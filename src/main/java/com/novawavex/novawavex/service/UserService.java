package com.novawavex.novawavex.service;

import com.novawavex.novawavex.dto.ProfileNameRequest;
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

    /*
     * =========================================
     * CREATE USER
     * =========================================
     */

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

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getProfileImage()
        );
    }

    /*
     * =========================================
     * GET ALL USERS
     * =========================================
     */

    public List<UserResponse> getAllUsers() {

        return userRepository
                .findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getProfileImage()
                ))
                .toList();

    }

    /*
     * =========================================
     * GET USER BY ID
     * =========================================
     */

    public UserResponse getUserById(Long id) {

        User user =
                userRepository
                        .findById(id)
                        .orElse(null);

        if (user == null) {

            return null;

        }

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImage()
        );
    }

    /*
     * =========================================
     * GET CURRENT AUTHENTICATED USER
     * =========================================
     */

    public UserResponse getCurrentUser(
            String email) {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImage()
        );
    }

    /*
     * =========================================
     * UPDATE CURRENT USER NAME
     * =========================================
     *
     * The email comes from the authenticated
     * JWT SecurityContext.
     *
     * The frontend does NOT provide the email.
     */

    @Transactional
    public UserResponse updateCurrentUserName(
            String email,
            ProfileNameRequest request) {

        /*
         * =====================================
         * FIND AUTHENTICATED USER
         * =====================================
         */

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        /*
         * =====================================
         * CLEAN NAME
         * =====================================
         */

        String fullName =
                request.getFullName()
                        .trim();

        /*
         * =====================================
         * VALIDATE NAME
         * =====================================
         */

        if (fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "Full name cannot be empty"
            );

        }

        /*
         * =====================================
         * UPDATE NAME
         * =====================================
         */

        user.setFullName(fullName);

        /*
         * =====================================
         * SAVE USER
         * =====================================
         */

        User updatedUser =
                userRepository.save(user);

        /*
         * =====================================
         * RETURN UPDATED PROFILE
         * =====================================
         */

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getProfileImage()
        );
    }
}