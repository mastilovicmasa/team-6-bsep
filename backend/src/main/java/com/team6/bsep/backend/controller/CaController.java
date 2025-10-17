package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CaInfo;
import com.team6.bsep.backend.dto.CertificateRequest;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.service.IntermediateCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ca")
@RequiredArgsConstructor
public class CaController {

    private final CertificateAuthorityRepository caRepo;
    private final IntermediateCertificateService certificateService;

    @GetMapping("/list")
    public List<CaInfo> listCas() {
        return caRepo.findAll().stream()
                .map(ca -> new CaInfo(
                        ca.getId(),
                        ca.getSubjectDn(),
                        ca.getNotBefore(),
                        ca.getNotAfter()
                ))
                .toList();
    }

    @PostMapping("/issue-intermediate")
    @PreAuthorize("hasAnyRole('ADMIN', 'CA')") // ✅ samo admin i CA korisnici mogu izdavati intermediate
    public ResponseEntity<CertificateAuthority> issueIntermediate(
            @RequestBody CertificateRequest dto,
            Principal principal   // ✅ automatski daje email trenutno ulogovanog korisnika
    ) {
        try {
            String issuerEmail = principal.getName(); // email iz tokena / sesije
            CertificateAuthority newCA = certificateService.issueIntermediateCertificate(dto, issuerEmail);
            return ResponseEntity.ok(newCA);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

}

