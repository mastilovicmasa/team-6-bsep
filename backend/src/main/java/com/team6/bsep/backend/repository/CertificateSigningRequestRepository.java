package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.CertificateSigningRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateSigningRequestRepository extends JpaRepository<CertificateSigningRequest, Long> {
}
