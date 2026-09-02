package com.novawavex.novawavex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    // =========================================
    // FULL NAME
    // =========================================

    @NotBlank(message = "Full name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;


    // =========================================
    // EMAIL
    // =========================================

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;


    // =========================================
    // PASSWORD
    // =========================================

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters"
    )
    private String password;


    // =========================================
    // CONFIRM PASSWORD
    // =========================================

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;


    // =========================================
    // PROFILE IMAGE
    // =========================================

    private String profileImage;


    // =========================================
    // DEFAULT CONSTRUCTOR
    // =========================================

    public RegisterRequest() {
    }


    // =========================================
    // GETTERS AND SETTERS
    // =========================================

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


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }


    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}