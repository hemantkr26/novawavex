package com.novawavex.novawavex.controller;

import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService) {

        this.userService = userService;

    }

    /*
     * =========================================
     * CREATE USER
     * =========================================
     */

    @PostMapping
    public UserResponse createUser(
            @Valid
            @RequestBody
            UserRequest request) {

        return userService.createUser(request);

    }

    /*
     * =========================================
     * GET ALL USERS
     * =========================================
     */

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();

    }

    /*
     * =========================================
     * GET CURRENT AUTHENTICATED USER
     * =========================================
     */

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName()
        );

    }

    /*
     * =========================================
     * UPDATE CURRENT USER NAME
     * =========================================
     *
     * Requires authenticated JWT.
     *
     * Email is taken from Authentication.
     * Frontend does NOT send the email.
     */

    @PutMapping("/me/name")
    public UserResponse updateCurrentUserName(

            @Valid
            @RequestBody
            ProfileNameRequest request,

            Authentication authentication) {

        String email =
                authentication.getName();

        return userService.updateCurrentUserName(
                email,
                request
        );
    }

    /*
     * =========================================
     * GET USER BY ID
     * =========================================
     */

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);

    }
}