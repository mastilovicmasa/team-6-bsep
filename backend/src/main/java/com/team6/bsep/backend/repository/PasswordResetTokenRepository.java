package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.PasswordResetToken;
import com.team6.bsep.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserAndUsedAtIsNull(User user);
}
