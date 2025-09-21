package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.service.CsrService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
