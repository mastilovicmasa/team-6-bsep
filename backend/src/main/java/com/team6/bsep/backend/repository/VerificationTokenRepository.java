package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);


    long deleteByExpiresAtBefore(Instant cutoff);
}
