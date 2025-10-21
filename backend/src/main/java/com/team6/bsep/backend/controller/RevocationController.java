package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.service.RevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/revoke")
@RequiredArgsConstructor
public class RevocationController {

    private final RevocationService service;

    @PostMapping("/{serialNumber}")
    public ResponseEntity<String> revokeCertificate(
            @PathVariable String serialNumber,
            @RequestParam String reason) {
        service.revokeCertificate(serialNumber, reason);
        return ResponseEntity.ok("Certificate revoked successfully");
    }

    @GetMapping("/status/{serialNumber}")
    public ResponseEntity<Boolean> checkRevoked(@PathVariable String serialNumber) {
        return ResponseEntity.ok(service.isRevoked(serialNumber));
    }
}
