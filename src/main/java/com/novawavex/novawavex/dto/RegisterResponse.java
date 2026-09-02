package com.novawavex.novawavex.dto;

public class RegisterResponse {

    // =========================================
    // USER INFORMATION
    // =========================================

    private Long id;

    private String fullName;

    private String email;

    private String role;

    private String profileImage;


    // =========================================
    // CONSTRUCTORS
    // =========================================

    public RegisterResponse() {
    }


    public RegisterResponse(
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
    // GETTERS AND SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}