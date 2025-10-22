package com.team6.bsep.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IssueCaRequest {

    @NotBlank(message = "Subject DN cannot be blank")
    @Pattern(
            regexp = "^[A-Za-z0-9,=\\s]+$",
            message = "Subject DN contains invalid characters"
    )
    private String subjectDn;

    @Min(value = 0, message = "Path length constraint cannot be negative")
    private int pathLenConstraint; // 0 = ne može dalje izdavati, 1 = može još jedan nivo
}
