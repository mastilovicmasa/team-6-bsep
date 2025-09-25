package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens",
        indexes = { @Index(name = "idx_prt_token", columnList = "token", unique = true),
                @Index(name = "idx_prt_user",  columnList = "user_id") })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant usedAt; // null dok nije iskorišćen
}
