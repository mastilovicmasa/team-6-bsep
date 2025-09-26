package com.team6.bsep.backend.dto;

public class JwtResponse {
    private String token;
    private int expiresIn;
    private String jti;
    private String role;
    private boolean mustChangePassword;

    public JwtResponse() {}

    public JwtResponse(String token, int expiresIn, String jti, String role, boolean mustChangePassword) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.jti = jti;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
