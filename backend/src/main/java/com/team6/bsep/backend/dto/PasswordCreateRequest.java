package com.team6.bsep.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordCreateRequest {
    private String siteName;
    private String username;
    private String encryptedPassword;
}
