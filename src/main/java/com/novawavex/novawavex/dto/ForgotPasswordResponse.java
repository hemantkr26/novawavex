package com.novawavex.novawavex.dto;

public class ForgotPasswordResponse {

    // =========================================
    // MESSAGE
    // =========================================

    private String message;

    // =========================================
    // DEFAULT CONSTRUCTOR
    // =========================================

    public ForgotPasswordResponse() {
    }

    // =========================================
    // MESSAGE CONSTRUCTOR
    // =========================================

    public ForgotPasswordResponse(String message) {
        this.message = message;
    }

    // =========================================
    // GETTER
    // =========================================

    public String getMessage() {
        return message;
    }

    // =========================================
    // SETTER
    // =========================================

    public void setMessage(String message) {
        this.message = message;
    }
}