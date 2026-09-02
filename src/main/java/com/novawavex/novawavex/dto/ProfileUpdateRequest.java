package com.novawavex.novawavex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;


    // =========================================
    // CONSTRUCTORS
    // =========================================

    public ProfileUpdateRequest() {

    }


    public ProfileUpdateRequest(
            String fullName
    ) {

        this.fullName = fullName;

    }


    // =========================================
    // GETTER
    // =========================================

    public String getFullName() {

        return fullName;

    }


    // =========================================
    // SETTER
    // =========================================

    public void setFullName(
            String fullName
    ) {

        this.fullName = fullName;

    }

}