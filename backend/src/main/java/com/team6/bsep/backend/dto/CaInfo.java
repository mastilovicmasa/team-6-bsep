package com.team6.bsep.backend.dto;

import java.time.Instant;

public class CaInfo {
    private Long id;
    private String subjectDn;
    private Instant notBefore;
    private Instant notAfter;

    public CaInfo(Long id, String subjectDn, Instant notBefore, Instant notAfter) {
        this.id = id;
        this.subjectDn = subjectDn;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectDn() {
        return subjectDn;
    }

    public void setSubjectDn(String subjectDn) {
        this.subjectDn = subjectDn;
    }

    public Instant getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Instant notBefore) {
        this.notBefore = notBefore;
    }

    public Instant getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Instant notAfter) {
        this.notAfter = notAfter;
    }
}
