package com.smartagriculture.dto;

import com.smartagriculture.entity.User;

public class AuthResponse {

    private boolean authenticated;
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private String preferredLanguage;
    private String message;

    public AuthResponse() {}

    public AuthResponse(User user, String message) {
        if (user != null) {
            this.authenticated = true;
            this.id = user.getId();
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.role = user.getRole();
            this.preferredLanguage = user.getPreferredLanguage();
        }
        this.message = message;
    }

    public static AuthResponse unauthenticated(String message) {
        AuthResponse res = new AuthResponse();
        res.setAuthenticated(false);
        res.setMessage(message);
        return res;
    }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
