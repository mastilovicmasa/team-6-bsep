package com.team6.bsep.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordShareRequest {

    @NotBlank(message = "Target email cannot be blank")
    @Email(message = "Target email must be valid")
    private String targetEmail;

    @NotBlank(message = "Encrypted password cannot be blank")
    private String encryptedPassword;
}
