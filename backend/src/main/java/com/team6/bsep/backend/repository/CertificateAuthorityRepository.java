package com.team6.bsep.backend.repository;


import com.team6.bsep.backend.dto.CaInfo;
import com.team6.bsep.backend.model.CertificateAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CertificateAuthorityRepository extends JpaRepository<CertificateAuthority, Long> {
    boolean existsByRootTrue();
    @Query("SELECT new com.team6.bsep.backend.dto.CaInfo(c.id, c.subjectDn, c.notBefore, c.notAfter) " +
            "FROM CertificateAuthority c WHERE c.subjectDn = :subjectDn")
    Optional<CaInfo> findProjectedBySubjectDn(String subjectDn);
}
