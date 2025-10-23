package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.service.RevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ca/revoke")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CaRevocationController {

    private final RevocationService revocationService;

    @PostMapping("/{serial}")
    public ResponseEntity<String> revokeCertificate(@PathVariable String serial,
                                                    @RequestBody(required = false) RevocationRequest req,
                                                    Authentication auth) {
        String email = auth.getName();
        String reason = req != null && req.getReason() != null ? req.getReason() : "unspecified";

        try {
            revocationService.revokeCertificateByCaUser(serial, email, reason);
            return ResponseEntity.ok("Certificate revoked successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error revoking certificate: " + e.getMessage());
        }
    }

    // ✅ DTO klasa unutar istog fajla (brzo rešenje)
    private static class RevocationRequest {
        private String reason;
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
