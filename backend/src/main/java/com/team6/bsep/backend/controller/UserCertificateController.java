package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.model.EndEntityCertificate;
import com.team6.bsep.backend.repository.EndEntityCertificateRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCertificateController {

    private final UserRepository userRepo;
    private final EndEntityCertificateRepository eeCertRepo;

    @GetMapping("/public-key/{userId}")
    public ResponseEntity<String> getPublicKey(@PathVariable Long userId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        var cert = eeCertRepo.findByCsr_User_Id(userId)
                .orElseThrow(() -> new RuntimeException("End-entity certificate not found for user: " + userId));

        return ResponseEntity.ok(cert.getPem());
    }
}
