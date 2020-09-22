package com.project.oauthservice.dto;

public class LoginResponse {
    private SuccessFailure status;
    private String message;

    public enum SuccessFailure {
        SUCCESS, FAILURE
    }

    public LoginResponse(SuccessFailure status, String message) {
        this.status = status;
        this.message = message;
    }

    public SuccessFailure getStatus() {
        return status;
    }

    public void setStatus(SuccessFailure status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
