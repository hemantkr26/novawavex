package com.novawavex.novawavex.controller;

import com.novawavex.novawavex.dto.AccountStatusUpdateRequest;
import com.novawavex.novawavex.dto.ProfileNameRequest;
import com.novawavex.novawavex.dto.RoleUpdateRequest;
import com.novawavex.novawavex.dto.UserRequest;
import com.novawavex.novawavex.dto.UserResponse;
import com.novawavex.novawavex.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // =========================================
    // CREATE USER
    // =========================================

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody UserRequest request) {

        return userService.createUser(request);
    }

    // =========================================
    // GET ALL USERS
    // =========================================

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================================
    // GET CURRENT AUTHENTICATED USER
    // =========================================

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName()
        );
    }

    // =========================================
    // UPDATE CURRENT USER NAME
    // =========================================

    @PutMapping("/me/name")
    public UserResponse updateCurrentUserName(
            @Valid @RequestBody ProfileNameRequest request,
            Authentication authentication) {

        String email =
                authentication.getName();

        return userService.updateCurrentUserName(
                email,
                request
        );
    }

    // =========================================
    // DELETE CURRENT AUTHENTICATED USER
    // =========================================
    //
    // Any authenticated user can delete
    // their own account.
    //

    @DeleteMapping("/me")
    public void deleteCurrentUser(
            Authentication authentication) {

        userService.deleteCurrentUser(
                authentication.getName()
        );
    }

    // =========================================
    // GET USER BY ID
    // =========================================

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // =========================================
    // CHANGE USER ROLE
    // =========================================
    //
    // ADMIN ONLY
    //
    // USER <-> ADMIN
    //

    @PutMapping("/{id}/role")
    public UserResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        return userService.updateUserRole(
                id,
                request
        );
    }

    // =========================================
    // ENABLE / DISABLE USER
    // =========================================
    //
    // ADMIN ONLY
    //

    @PutMapping("/{id}/status")
    public UserResponse updateAccountStatus(
            @PathVariable Long id,
            @RequestBody AccountStatusUpdateRequest request) {

        return userService.updateAccountStatus(
                id,
                request
        );
    }

    // =========================================
    // DELETE USER
    // =========================================
    //
    // ADMIN ONLY
    //
    // An ADMIN cannot use this endpoint to
    // delete their own account.
    //
    // Their own account must be deleted
    // through DELETE /api/users/me.
    //

    @DeleteMapping("/{id}")
    public void deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        userService.deleteUser(
                id,
                authentication.getName()
        );
    }
}