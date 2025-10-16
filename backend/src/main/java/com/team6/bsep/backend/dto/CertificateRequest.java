package com.team6.bsep.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    private Long issuerId;          // ID CA koji potpisuje novi sertifikat
    private String commonName;      // CN
    private String organization;    // O
    private String organizationalUnit; // OU
    private String country;         // C
    private String email;           // E
    private int validityInDays;
}
