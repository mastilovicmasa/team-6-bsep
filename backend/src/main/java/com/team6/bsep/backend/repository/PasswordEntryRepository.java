package com.team6.bsep.backend.repository;

import com.team6.bsep.backend.model.PasswordEntry;
import com.team6.bsep.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

    // Sve lozinke koje je korisnik kreirao
    List<PasswordEntry> findByOwner(User owner);

    // Lozinke sa kojima je korisnik podeljen (preko PasswordShare)
    @Query("""
        SELECT DISTINCT ps.passwordEntry
        FROM PasswordShare ps
        WHERE ps.user = :user
        """)
    List<PasswordEntry> findSharedWithUser(User user);
}
