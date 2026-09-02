package com.novawavex.novawavex.dto;

public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String role;

    private String profileImage;


    // =========================================
    // CONSTRUCTORS
    // =========================================

    public UserResponse() {

    }


    public UserResponse(
            Long id,
            String fullName,
            String email,
            String role
    ) {

        this.id = id;

        this.fullName = fullName;

        this.email = email;

        this.role = role;
    }


    public UserResponse(
            Long id,
            String fullName,
            String email,
            String role,
            String profileImage
    ) {

        this.id = id;

        this.fullName = fullName;

        this.email = email;

        this.role = role;

        this.profileImage = profileImage;
    }


    // =========================================
    // GETTERS
    // =========================================

    public Long getId() {

        return id;
    }


    public String getFullName() {

        return fullName;
    }


    public String getEmail() {

        return email;
    }


    public String getRole() {

        return role;
    }


    public String getProfileImage() {

        return profileImage;
    }
}