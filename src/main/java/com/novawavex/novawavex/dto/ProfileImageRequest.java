package com.novawavex.novawavex.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileImageRequest {

    @NotBlank(message = "Profile image is required")
    private String profileImage;


    // =========================================
    // CONSTRUCTORS
    // =========================================

    public ProfileImageRequest() {

    }


    public ProfileImageRequest(
            String profileImage
    ) {

        this.profileImage = profileImage;

    }


    // =========================================
    // GETTER
    // =========================================

    public String getProfileImage() {

        return profileImage;

    }


    // =========================================
    // SETTER
    // =========================================

    public void setProfileImage(
            String profileImage
    ) {

        this.profileImage = profileImage;

    }

}