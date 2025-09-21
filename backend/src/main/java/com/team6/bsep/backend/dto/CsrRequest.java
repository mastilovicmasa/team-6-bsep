package com.team6.bsep.backend.dto;

import org.springframework.web.multipart.MultipartFile;

public class CsrRequest {
    private String caName;
    private int durationInDays;
    private MultipartFile csrFile;

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
