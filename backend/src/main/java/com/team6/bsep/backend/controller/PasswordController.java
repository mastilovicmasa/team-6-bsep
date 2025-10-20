package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.*;
import com.team6.bsep.backend.model.PasswordEntry;
import com.team6.bsep.backend.model.PasswordShare;
import com.team6.bsep.backend.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    // 1. Kreiranje nove lozinke
    @PostMapping
    public ResponseEntity<PasswordEntryDTO> createPassword(@RequestBody PasswordCreateRequest request) {
        PasswordEntry entry = passwordService.createPasswordEntry(
                request.getSiteName(),
                request.getUsername(),
                request.getEncryptedPassword()
        );

        return ResponseEntity.ok(toDTO(entry));
    }

    // 2. Deljenje lozinke sa drugim korisnikom
    @PostMapping("/{entryId}/share")
    public ResponseEntity<PasswordShareDTO> sharePassword(
            @PathVariable Long entryId,
            @RequestBody PasswordShareRequest request) {

        PasswordShare share = passwordService.sharePassword(
                entryId,
                request.getTargetUserId(),
                request.getEncryptedPassword()
        );

        return ResponseEntity.ok(toDTO(share));
    }

    // 3. Vraća sve lozinke vidljive korisniku (njegove + deljene)
    @GetMapping
    public ResponseEntity<List<PasswordEntryDTO>> getVisiblePasswords() {
        List<PasswordEntryDTO> list = passwordService.getVisiblePasswords()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    // 4. Vraća jedan entry sa svim shareovima
    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntryDTO> getPasswordEntry(@PathVariable Long id) {
        PasswordEntry entry = passwordService.getPasswordEntry(id);
        return ResponseEntity.ok(toDTO(entry));
    }

    // 5. Opoziva deljenje lozinke
    @DeleteMapping("/share/{shareId}")
    public ResponseEntity<Void> revokeShare(@PathVariable Long shareId) {
        passwordService.revokeShare(shareId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------
    // DTO maperi
    // ------------------------

    private PasswordEntryDTO toDTO(PasswordEntry entry) {
        return PasswordEntryDTO.builder()
                .id(entry.getId())
                .siteName(entry.getSiteName())
                .username(entry.getUsername())
                .ownerId(entry.getOwner().getId())
                .createdAt(entry.getCreatedAt())
                .shares(entry.getShares() != null
                        ? entry.getShares().stream().map(this::toDTO).collect(Collectors.toList())
                        : List.of())
                .build();
    }

    private PasswordShareDTO toDTO(PasswordShare share) {
        return PasswordShareDTO.builder()
                .id(share.getId())
                .userId(share.getUser().getId())
                .encryptedPassword(share.getEncryptedPassword())
                .sharedAt(share.getSharedAt())
                .build();
    }
}
