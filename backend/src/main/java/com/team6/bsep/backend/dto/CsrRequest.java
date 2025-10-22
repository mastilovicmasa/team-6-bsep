package com.team6.bsep.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class CsrRequest {
    @NotBlank(message = "CA name cannot be blank")
    private String caName;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private int durationInDays;

    @NotNull(message = "CSR file must be provided")
    private MultipartFile csrFile;

    public CsrRequest(String caName, int durationInDays, MultipartFile csrFile) {
        this.caName = caName;
        this.durationInDays = durationInDays;
        this.csrFile = csrFile;
    }

    public String getCaName() {
        return caName;
    }

    public void setCaName(String caName) {
        this.caName = caName;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public MultipartFile getCsrFile() {
        return csrFile;
    }

    public void setCsrFile(MultipartFile csrFile) {
        this.csrFile = csrFile;
    }
}
