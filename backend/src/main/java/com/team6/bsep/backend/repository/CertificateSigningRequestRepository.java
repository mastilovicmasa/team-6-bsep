package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.dto.MyCsr;
import com.team6.bsep.backend.model.CertificateSigningRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CertificateSigningRequestRepository extends JpaRepository<CertificateSigningRequest, Long> {
    @Query("SELECT new com.team6.bsep.backend.dto.MyCsr(" +
            "r.id, r.subjectCn, r.subjectO, r.subjectC, r.durationInDays, r.status, r.createdAt) " +
            "FROM CertificateSigningRequest r " +
            "WHERE r.user.email = :email")
    List<MyCsr> findMyRequestsByUserEmail(String email);

    @Query("SELECT new com.team6.bsep.backend.dto.MyCsr(" +
            "r.id, r.subjectCn, r.subjectO, r.subjectC, r.durationInDays, r.status, r.createdAt) " +
            "FROM CertificateSigningRequest r")
    List<MyCsr> findAllRequests();

}
