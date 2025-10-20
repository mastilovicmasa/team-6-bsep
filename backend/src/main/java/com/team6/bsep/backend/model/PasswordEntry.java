package com.team6.bsep.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "password_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String siteName;
    private String username;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;  // EE korisnik (iz PKI sistema)

    private LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(mappedBy = "passwordEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordShare> shares;

}
