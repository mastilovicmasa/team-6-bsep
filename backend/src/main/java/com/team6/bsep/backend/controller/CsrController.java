package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.dto.MyCsr;
import com.team6.bsep.backend.model.CertificateSigningRequest;
import com.team6.bsep.backend.service.CsrService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/csr")
@RequiredArgsConstructor
public class CsrController {

    private static final Logger log = LoggerFactory.getLogger(CsrController.class);

    private final CsrService csrService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsr(@ModelAttribute CsrRequest request) {
        try {
            log.info("CA = {}, duration = {}, file = {}",
                    request.getCaName(),
                    request.getDurationInDays(),
                    request.getCsrFile() != null ? request.getCsrFile().getOriginalFilename() : "null"
            );
            csrService.processCsr(request);
            return ResponseEntity.ok(Map.of("message", "CSR processed successfully!"));
        } catch (Exception e) {
            log.error("CSR upload failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<MyCsr>> getMyRequests(Authentication auth) {
        return ResponseEntity.ok(csrService.getRequestsForUser(auth.getName()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MyCsr>> getAllRequests() {
        return ResponseEntity.ok(csrService.getAllRequests());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCsr(@PathVariable Long id) {
        try {
            csrService.approveRequest(id);
            return ResponseEntity.ok(Map.of("message", "CSR approved and certificate issued"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/certificates/{csrId}/download")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long csrId) {
        byte[] pemBytes = csrService.getCertificatePem(csrId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate-" + csrId + ".crt")
                .contentType(MediaType.valueOf("application/x-pem-file"))
                .body(pemBytes);
    }

}
