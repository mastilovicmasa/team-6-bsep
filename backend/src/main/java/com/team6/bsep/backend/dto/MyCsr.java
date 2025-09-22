package com.team6.bsep.backend.dto;

import com.team6.bsep.backend.model.RequestStatus;
import java.time.Instant;

public record MyCsr(
        Long id,
        String subjectCn,
        String subjectO,
        String subjectC,
        int durationInDays,
        RequestStatus status,
        Instant createdAt
) {}
