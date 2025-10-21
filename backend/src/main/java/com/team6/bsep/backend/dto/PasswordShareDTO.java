package com.team6.bsep.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordShareDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String encryptedPassword;
    private LocalDateTime sharedAt;
}
