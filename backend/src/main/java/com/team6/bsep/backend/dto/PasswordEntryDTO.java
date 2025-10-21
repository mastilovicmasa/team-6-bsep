package com.team6.bsep.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordEntryDTO {
    private Long id;
    private String siteName;
    private String username;
    private Long ownerId;
    private LocalDateTime createdAt;
    private String encryptedPassword;
    private List<PasswordShareDTO> shares;
}
