package com.team6.bsep.backend.dto;

import com.team6.bsep.backend.model.CertificateTemplate;
import lombok.Data;

@Data
public class TemplateResponse {
    private Long id;
    private String name;
    private String cnRegex;
    private String sanRegex;
    private Integer ttlDays;

    private String issuerCaName; // npr. DN izdavaoca

    public TemplateResponse(CertificateTemplate t) {
        this.id = t.getId();
        this.name = t.getName();
        this.cnRegex = t.getCnRegex();
        this.sanRegex = t.getSanRegex();
        this.ttlDays = t.getTtlDays();
        this.issuerCaName = t.getIssuerCa() != null ? t.getIssuerCa().getSubjectDn() : "Unknown";
    }
}
