package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.PasswordEntryDTO;
import com.team6.bsep.backend.dto.PasswordShareDTO;
import com.team6.bsep.backend.dto.SharedPasswordDecryptView;
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

        PasswordEntry entry = PasswordEntry.builder()
                .siteName(siteName)
                .username(username)
                .owner(owner)
                .encryptedPassword(encryptedPassword)
                .createdAt(LocalDateTime.now())
                .build();

        return entryRepo.save(entry);
    }

    // Deljenje lozinke sa drugim korisnikom (po EMAIL-u)
    @Transactional
    public PasswordShare sharePassword(Long entryId, String targetEmail, String encryptedPasswordForTarget) {
        User currentUser = getCurrentUser();

        if (currentUser.getEmail().equalsIgnoreCase(targetEmail)) {
            throw new RuntimeException("You cannot share a password with yourself.");
        }

        PasswordEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Password entry not found: " + entryId));

        if (!entry.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can share this password");
        }

        User targetUser = userRepo.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("Target user not found with email: " + targetEmail));

        // sprecavanje duplikat share-a
        boolean alreadyShared = shareRepo.existsByPasswordEntryAndUser(entry, targetUser);
        if (alreadyShared) {
            throw new RuntimeException("Password already shared with this user");
        }

        PasswordShare share = PasswordShare.builder()
                .passwordEntry(entry)
                .user(targetUser)
                .encryptedPassword(encryptedPasswordForTarget)
                .sharedAt(LocalDateTime.now())
                .build();

        return shareRepo.save(share);
    }


    @Transactional(readOnly = true)
    public List<PasswordEntry> getMyPasswords() {
        User user = getCurrentUser();
        return entryRepo.findByOwner(user);
    }

    @Transactional(readOnly = true)
    public List<SharedPasswordDecryptView> getSharedPasswordsForUser() {
        User user = getCurrentUser();

        return shareRepo.findAllByUser(user)
                .stream()
                .map(ps -> SharedPasswordDecryptView.builder()
                        .shareId(ps.getId())
                        .siteName(ps.getPasswordEntry().getSiteName())
                        .username(ps.getPasswordEntry().getUsername())
                        .encryptedPassword(ps.getEncryptedPassword())
                        .sharedAt(ps.getSharedAt())
                        .ownerEmail(ps.getPasswordEntry().getOwner().getEmail())
                        .build())
                .toList();
    }



    // Dohvati jedan password (ako je vlasnik ili ako mu je podeljen)
    @Transactional(readOnly = true)
    public PasswordEntryDTO getPasswordEntry(Long id) {
        User currentUser = getCurrentUser();

        PasswordEntry entry = entryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Password entry not found: " + id));

        boolean isOwner = entry.getOwner().getId().equals(currentUser.getId());
        boolean isShared = shareRepo.existsByPasswordEntryAndUser(entry, currentUser);

        if (!isOwner && !isShared) {
            throw new RuntimeException("Access denied");
        }

        // Odredi enkriptovanu lozinku koju korisnik sme da vidi
        String encrypted;
        if (isOwner) {
            encrypted = entry.getEncryptedPassword();
        } else {
            encrypted = shareRepo.findByPasswordEntryAndUser(entry, currentUser)
                    .map(PasswordShare::getEncryptedPassword)
                    .orElseThrow(() -> new RuntimeException("Share not found for user"));
        }

        // Mapiraj share-ove (ako ih ima)
        List<PasswordShareDTO> shares = entry.getShares() != null
                ? entry.getShares().stream()
                .map(s -> PasswordShareDTO.builder()
                        .id(s.getId())
                        .userId(s.getUser().getId())
                        .userEmail(s.getUser().getEmail())
                        .encryptedPassword(s.getEncryptedPassword())
                        .sharedAt(s.getSharedAt())
                        .build())
                .toList()
                : List.of();

        return PasswordEntryDTO.builder()
                .id(entry.getId())
                .siteName(entry.getSiteName())
                .username(entry.getUsername())
                .ownerId(entry.getOwner().getId())
                .createdAt(entry.getCreatedAt())
                .encryptedPassword(encrypted)
                .shares(shares)
                .build();
    }


    // Povlačenje deljenja
    @Transactional
    public void revokeShare(Long shareId) {
        User user = getCurrentUser();
        PasswordShare share = shareRepo.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found: " + shareId));

        if (!share.getPasswordEntry().getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only the owner can revoke a share");
        }

        shareRepo.delete(share);
    }
}
