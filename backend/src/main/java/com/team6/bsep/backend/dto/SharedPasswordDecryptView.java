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
public class SharedPasswordDecryptView {
    private Long shareId;
    private String siteName;
    private String username;
    private String ownerEmail;
    private String encryptedPassword;
    private LocalDateTime sharedAt;
}

