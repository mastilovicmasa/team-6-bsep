package com.team6.bsep.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordDecryptView {
    private Long id;
    private String siteName;
    private String username;
    private String encryptedPassword; // uvek onaj koji korisnik može da dekriptuje
    private LocalDateTime createdAt;
}

