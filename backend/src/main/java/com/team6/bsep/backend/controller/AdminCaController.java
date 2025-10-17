package com.team6.bsep.backend.controller;



import com.team6.bsep.backend.dto.CaUserResponse;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.repository.UserRepository;
import com.team6.bsep.backend.service.CaService;
import com.team6.bsep.backend.service.RootCaService;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
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
            @RequestBody Map<String, String> body) {
        try {
            String subjectDn = body.get("subjectDn");
            caService.issueCaCertificateForUser(email, subjectDn);
            return ResponseEntity.ok(Map.of("message", "CA certificate issued successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

