package com.team6.bsep.backend.model;

import java.time.LocalDateTime;

public class TokenInfo {
    private String jti;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActivity;

    public TokenInfo(String jti, String ipAddress, String userAgent, LocalDateTime lastActivity) {
        this.jti = jti;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.lastActivity = lastActivity;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
}
