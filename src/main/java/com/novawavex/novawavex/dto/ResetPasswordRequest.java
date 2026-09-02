package com.novawavex.novawavex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(
            min = 8,
            message = "Password must contain at least 8 characters"
    )
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /*
     * =========================================
     * CONSTRUCTORS
     * =========================================
     */

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(
            String resetToken,
            String newPassword,
            String confirmPassword) {

        this.resetToken = resetToken;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    /*
     * =========================================
     * GETTERS AND SETTERS
     * =========================================
     */

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(
            String confirmPassword) {

        this.confirmPassword = confirmPassword;
    }
}