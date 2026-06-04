package com.gradecalculator.dto.response;

/**
 * Login response DTO with JWT access token and optional refresh token.
 */
public class LoginResponse {

    private Long userId;
    private String username;
    private String role;
    private String token;
    private String tokenType = "Bearer";
    private boolean mustChangePassword;
    /** Refresh token — only present on login, not on /me */
    private String refreshToken;

    public LoginResponse() {}

    public LoginResponse(Long userId, String username, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
        this.mustChangePassword = false;
    }

    public LoginResponse(Long userId, String username, String role, String token, boolean mustChangePassword) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
        this.mustChangePassword = mustChangePassword;
    }

    public LoginResponse(Long userId, String username, String role, String token,
                         boolean mustChangePassword, String refreshToken) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.token = token;
        this.mustChangePassword = mustChangePassword;
        this.refreshToken = refreshToken;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getId() { return userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
