package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.PasswordEntry;
import com.team6.bsep.backend.model.PasswordShare;
import com.team6.bsep.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PasswordShareRepository extends JpaRepository<PasswordShare, Long> {

    // Sve instance deljenja za jedan password entry
    List<PasswordShare> findByPasswordEntry(PasswordEntry entry);

    // Lozinka podeljena sa konkretnim korisnikom
    Optional<PasswordShare> findByPasswordEntryAndUser(PasswordEntry entry, User user);

    boolean existsByPasswordEntryAndUser(PasswordEntry entry, User user);

    // Sve lozinke podeljene korisniku
    List<PasswordShare> findByUser(User user);

    @Query("""
    SELECT ps
    FROM PasswordShare ps
    JOIN FETCH ps.passwordEntry pe
    WHERE ps.user = :user
    """)
    List<PasswordShare> findAllByUser(@Param("user") User user);
}
