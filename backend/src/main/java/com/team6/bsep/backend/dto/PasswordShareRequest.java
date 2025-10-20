package com.team6.bsep.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordShareRequest {
    private Long targetUserId;
    private String encryptedPassword;
}
