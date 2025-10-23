package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    List<CertificateTemplate> findByIssuerCa(CertificateAuthority issuerCa);
    List<CertificateTemplate> findAllByIssuerCaIn(List<CertificateAuthority> cas);
}
