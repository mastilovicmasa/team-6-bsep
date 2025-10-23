package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class CertificateTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String cnRegex;
    String sanRegex;
    Integer ttlDays;
    Integer keyUsageMask;

    @ElementCollection(fetch = FetchType.EAGER)
    Set<String> extendedKeyUsages;

    @ManyToOne
    CertificateAuthority issuerCa;
}
