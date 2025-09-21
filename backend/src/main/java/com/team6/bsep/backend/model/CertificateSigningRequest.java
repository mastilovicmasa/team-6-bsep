package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certificate_signing_request")
public class CertificateSigningRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String csrPem;   // ceo CSR u PEM formatu

    @Column(nullable = false)
    private int durationInDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // PENDING, ISSUED, REJECTED

    @ManyToOne(optional = false)
    private CertificateAuthority ca;

    // parsed podaci iz CSR (nije obavezno, ali korisno za pretragu)
    private String subjectCn;
    private String subjectO;
    private String subjectC;

    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCsrPem() {
        return csrPem;
    }

    public void setCsrPem(String csrPem) {
        this.csrPem = csrPem;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public CertificateAuthority getCa() {
        return ca;
    }

    public void setCa(CertificateAuthority ca) {
        this.ca = ca;
    }

    public String getSubjectCn() {
        return subjectCn;
    }

    public void setSubjectCn(String subjectCn) {
        this.subjectCn = subjectCn;
    }

    public String getSubjectO() {
        return subjectO;
    }

    public void setSubjectO(String subjectO) {
        this.subjectO = subjectO;
    }

    public String getSubjectC() {
        return subjectC;
    }

    public void setSubjectC(String subjectC) {
        this.subjectC = subjectC;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
