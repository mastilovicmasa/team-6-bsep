package com.team6.bsep.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordCreateRequest {

    @NotBlank(message = "Site name cannot be blank")
    @Size(max = 100, message = "Site name must be up to 100 characters")
    private String siteName;

    @NotBlank(message = "Username cannot be blank")
    @Size(max = 100, message = "Username must be up to 100 characters")
    private String username;

    @NotBlank(message = "Encrypted password cannot be blank")
    private String encryptedPassword;
}

