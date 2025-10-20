package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referenca na lozinku
    @ManyToOne
    @JoinColumn(name = "entry_id", nullable = false)
    private PasswordEntry passwordEntry;

    // kome je podeljeno
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String encryptedPassword;

    private LocalDateTime sharedAt = LocalDateTime.now();


}
