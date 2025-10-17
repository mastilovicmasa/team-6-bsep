package com.team6.bsep.backend.dto;

import lombok.Data;

@Data
public class IssueCaRequest {
    private String subjectDn;
    private int pathLenConstraint; // 0 = ne može dalje izdavati, 1 = može još jedan nivo
}
