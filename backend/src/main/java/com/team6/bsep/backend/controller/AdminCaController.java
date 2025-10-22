package com.team6.bsep.backend.controller;



import com.team6.bsep.backend.dto.CaUserResponse;
import com.team6.bsep.backend.dto.IssueCaRequest;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.repository.UserRepository;
import com.team6.bsep.backend.service.CaService;
import com.team6.bsep.backend.service.RootCaService;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ca")
@RequiredArgsConstructor
public class AdminCaController {

    private final RootCaService rootCaService;
    private final CertificateAuthorityRepository caRepo;
    private final CaService caService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/root")
    public ResponseEntity<?> createRoot() throws Exception {
        if (caRepo.existsByRootTrue()) {
            return ResponseEntity.status(409).body("Root CA already exists.");
        }
        rootCaService.createRootIfMissing();
        return ResponseEntity.status(201).body("Root CA created.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<CaUserResponse>> getAllCaUsers() {
        List<CaUserResponse> users = caService.getAllCaUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{email}/issue-ca")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> issueCaCertificate(
            @PathVariable String email,
            @Valid @RequestBody IssueCaRequest body) {
        try {
            String subjectDn = body.getSubjectDn();
            int pathLen = body.getPathLenConstraint();

            caService.issueCaCertificateForUser(email, subjectDn, pathLen);

            return ResponseEntity.ok(
                    java.util.Map.of("message", "CA certificate issued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}

