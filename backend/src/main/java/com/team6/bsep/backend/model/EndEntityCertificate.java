package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndEntityCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serialHex;

    @Lob
    private String pem;

    private Instant notBefore;
    private Instant notAfter;

    @ManyToOne
    private CertificateAuthority issuer;

    @ManyToOne
    private CertificateSigningRequest csr;
}
