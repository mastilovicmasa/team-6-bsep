package com.team6.bsep.backend.model;

import com.team6.bsep.backend.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "verification_tokens",
        indexes = {
                @Index(name = "idx_verification_token_token", columnList = "token", unique = true),
                @Index(name = "idx_verification_token_expires_at", columnList = "expires_at")
        }
)
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Jedinstveni token (UUID ili crypto-random);
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    // Korisnik kome pripada token; LAZY da se ne učitava bespotrebno
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_token_user")
    )
    private User user;

    // Rok važenja linka
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Popunjava se kada je link iskorišćen (jednokratnost)
    @Column(name = "used_at")
    private Instant usedAt;


    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed() { return usedAt != null; }
}
