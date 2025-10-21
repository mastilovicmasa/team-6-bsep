package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.RevokedCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevokedCertificateRepository extends JpaRepository<RevokedCertificate, Long> {
    Optional<RevokedCertificate> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);
}

