package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.EndEntityCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EndEntityCertificateRepository extends JpaRepository<EndEntityCertificate, Long> {
    Optional<EndEntityCertificate> findByCsrId(Long csrId);
    Optional<EndEntityCertificate> findByCsr_User_Id(Long userId);
}

