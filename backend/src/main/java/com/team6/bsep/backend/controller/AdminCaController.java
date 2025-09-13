package com.team6.bsep.backend.controller;



import com.team6.bsep.backend.service.RootCaService;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ca")
@RequiredArgsConstructor
public class AdminCaController {

    private final RootCaService rootCaService;
    private final CertificateAuthorityRepository caRepo;

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/root")
    public ResponseEntity<?> createRoot() throws Exception {
        if (caRepo.existsByRootTrue()) {
            return ResponseEntity.status(409).body("Root CA already exists.");
        }
        rootCaService.createRootIfMissing();
        return ResponseEntity.status(201).body("Root CA created.");
    }
}

