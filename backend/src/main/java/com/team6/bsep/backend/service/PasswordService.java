package com.team6.bsep.backend.service;

import com.team6.bsep.backend.model.PasswordEntry;
import com.team6.bsep.backend.model.PasswordShare;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.repository.PasswordEntryRepository;
import com.team6.bsep.backend.repository.PasswordShareRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEntryRepository entryRepo;
    private final PasswordShareRepository shareRepo;
    private final UserRepository userRepo;

    // HELPER: trenutno prijavljeni korisnik
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails userDetails)
                ? userDetails.getUsername()
                : principal.toString();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }


    // Kreiranje nove lozinke
    @Transactional
    public PasswordEntry createPasswordEntry(String siteName, String username, String encryptedPassword) {
        User owner = getCurrentUser();

        // kreiramo entry
        PasswordEntry entry = PasswordEntry.builder()
                .siteName(siteName)
                .username(username)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        // kreiramo share za vlasnika (da i on ima pristup svojoj lozinki)
        PasswordShare ownerShare = PasswordShare.builder()
                .passwordEntry(entry)
                .user(owner)
                .encryptedPassword(encryptedPassword)
                .sharedAt(LocalDateTime.now())
                .build();

        entry.setShares(List.of(ownerShare));

        return entryRepo.save(entry);
    }


    // Deljenje lozinke sa drugim korisnikom
    @Transactional
    public PasswordShare sharePassword(Long entryId, Long targetUserId, String encryptedPasswordForTarget) {
        User currentUser = getCurrentUser();

        PasswordEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Password entry not found: " + entryId));

        // sigurnosna provera — samo vlasnik može deliti
        if (!entry.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can share this password");
        }

        User targetUser = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found: " + targetUserId));

        // kreiramo novi share
        PasswordShare share = PasswordShare.builder()
                .passwordEntry(entry)
                .user(targetUser)
                .encryptedPassword(encryptedPasswordForTarget)
                .sharedAt(LocalDateTime.now())
                .build();

        return shareRepo.save(share);
    }


    // Prikaz svih lozinki za korisnika
    @Transactional(readOnly = true)
    public List<PasswordEntry> getVisiblePasswords() {
        User user = getCurrentUser();
        List<PasswordEntry> owned = entryRepo.findByOwner(user);
        List<PasswordEntry> shared = entryRepo.findSharedWithUser(user);

        // objedini i ukloni duplikate
        return List.copyOf(
                java.util.stream.Stream.concat(owned.stream(), shared.stream())
                        .distinct()
                        .toList()
        );
    }


    // Dohvati jednu lozinku (sa shareovima)
    @Transactional(readOnly = true)
    public PasswordEntry getPasswordEntry(Long id) {
        User user = getCurrentUser();

        PasswordEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Password entry not found: " + id));

        // korisnik može videti ako je vlasnik ili ako mu je podeljeno
        boolean isOwner = entry.getOwner().getId().equals(user.getId());
        boolean isShared = entry.getShares().stream()
                .anyMatch(s -> s.getUser().getId().equals(user.getId()));

        if (!isOwner && !isShared) {
            throw new RuntimeException("Access denied to this password entry");
        }

        return entry;
    }


    // Uklanjanje deljenja (revoke share)
    @Transactional
    public void revokeShare(Long shareId) {
        User user = getCurrentUser();
        PasswordShare share = shareRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found: " + shareId));

        // samo vlasnik password-a može povući deljenje
        if (!share.getPasswordEntry().getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only the owner can revoke a share");
        }

        shareRepo.delete(share);
    }
}
