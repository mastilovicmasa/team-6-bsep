package com.team6.bsep.backend.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RevokedCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String reason;  //  "keyCompromise", "cessationOfOperation"

    @Column(nullable = false)
    private LocalDateTime revokedAt = LocalDateTime.now();

    public RevokedCertificate() {}

    public RevokedCertificate(String serialNumber, String reason) {
        this.serialNumber = serialNumber;
        this.reason = reason;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
