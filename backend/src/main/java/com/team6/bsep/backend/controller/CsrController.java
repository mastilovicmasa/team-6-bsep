package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.service.CsrParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/csr")
public class CsrController {

    private static final Logger log = LoggerFactory.getLogger(CsrController.class);

    private final CsrParserService csrParserService;

    public CsrController(CsrParserService csrParserService) {
        this.csrParserService = csrParserService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsr(@ModelAttribute CsrRequest request) {
        try {
            String caName = request.getCaName();
            int duration = request.getDurationInDays();

            log.info("Received CSR upload: CA = {}, duration = {} days, file = {}",
                    caName, duration,
                    request.getCsrFile() != null ? request.getCsrFile().getOriginalFilename() : "null");

            if (request.getCsrFile() == null || request.getCsrFile().isEmpty()) {
                return ResponseEntity.badRequest().body("CSR file is required.");
            }

            csrParserService.parseAndLog(request.getCsrFile());

            return ResponseEntity.ok("CSR uploaded and parsed successfully.");
        } catch (Exception e) {
            log.error("Error while processing CSR upload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid CSR: " + e.getMessage());
        }
    }
}
