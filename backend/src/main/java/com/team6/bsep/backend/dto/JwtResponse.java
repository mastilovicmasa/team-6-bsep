package com.team6.bsep.backend.dto;

public class JwtResponse {
    private String token;
    private int expiresIn;
    private String jti;

    public JwtResponse() {}

    public JwtResponse(String token, int expiresIn, String jti) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.jti = jti;
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
}
