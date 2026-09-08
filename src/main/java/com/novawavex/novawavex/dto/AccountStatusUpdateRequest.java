package com.novawavex.novawavex.dto;

public class AccountStatusUpdateRequest {

    private boolean enabled;

    public AccountStatusUpdateRequest() {
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }
}