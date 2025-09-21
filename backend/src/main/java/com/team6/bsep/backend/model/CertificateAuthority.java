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
@Table(name="certificate_authority",
        indexes = @Index(name="idx_ca_serial", columnList="serialHex", unique=true))
public class CertificateAuthority {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    boolean root;
    String subjectDn; String serialHex;
    Instant notBefore; Instant notAfter;
    Integer pathLenConstraint;
    String keystorePath; String keystoreAlias;
    //@Lob
    String keystorePasswordEnc;
    //@Lob
    String keyPasswordEnc;
    Instant revokedAt;
}

