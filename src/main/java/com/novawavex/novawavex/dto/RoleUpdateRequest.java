package com.novawavex.novawavex.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleUpdateRequest {

    @NotBlank(message = "Role is required")
    private String role;

    public RoleUpdateRequest() {
    }

    public String getRole() {

        return role;
    }

    public void setRole(String role) {

        this.role = role;
    }
}