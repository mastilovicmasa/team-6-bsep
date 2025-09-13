package com.team6.bsep.backend.repository;


import com.team6.bsep.backend.model.CertificateAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateAuthorityRepository extends JpaRepository<CertificateAuthority, Long> {
    boolean existsByRootTrue();
}
