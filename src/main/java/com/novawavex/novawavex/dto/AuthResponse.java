package com.novawavex.novawavex.dto;

public class AuthResponse {

    private String token;
    private String tokenType;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}