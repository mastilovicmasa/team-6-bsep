package com.team6.bsep.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaUserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String organization;
    private boolean hasCaCertificate;
}
