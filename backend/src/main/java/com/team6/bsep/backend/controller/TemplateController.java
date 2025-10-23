package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.TemplateResponse;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.CertificateTemplate;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.CertificateTemplateRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ca/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TemplateController {

    private final CertificateTemplateRepository tplRepo;
    private final CertificateAuthorityRepository caRepo;
    private final UserRepository userRepo;

    @Transactional
    @GetMapping("/all")
    public List<CertificateTemplate> listAll() {
        return tplRepo.findAll();
    }

    @Transactional
    @PostMapping
    public CertificateTemplate create(@RequestBody CertificateTemplate req, Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CertificateAuthority issuerCa = null;

        if (user.getCertificateAuthority() != null) {
            issuerCa = user.getCertificateAuthority();
        } else if (user.getRole().name().equalsIgnoreCase("ADMIN")) {
            issuerCa = caRepo.findByRootTrue()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Root CA not found"));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any CA certificate and is not admin");
        }

        req.setIssuerCa(issuerCa);
        return tplRepo.save(req);
    }
    @Transactional
    @GetMapping
    public List<TemplateResponse> listMyTemplates(Authentication auth) {
        String email = auth.getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        CertificateAuthority issuerCa = user.getCertificateAuthority();

        // Ako je admin — vidi sve
        if (issuerCa == null && user.getRole().name().equalsIgnoreCase("ADMIN")) {
            return tplRepo.findAll()
                    .stream()
                    .map(TemplateResponse::new)
                    .toList();
        }

        // Ako nije CA user — nema pristup
        if (issuerCa == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any CA certificate");
        }

        // ✳️ Dodato: CA user vidi svoje i od svog issuer-a (nadređenog CA)
        CertificateAuthority parentCa = issuerCa.getIssuer();

        List<CertificateTemplate> templates;

        if (parentCa != null) {
            templates = tplRepo.findAllByIssuerCaIn(List.of(issuerCa, parentCa));
        } else {
            templates = tplRepo.findByIssuerCa(issuerCa);
        }

        return templates.stream()
                .map(TemplateResponse::new)
                .toList();
    }

    @Transactional
    @GetMapping("/issuer/{issuerId}")
    public List<CertificateTemplate> list(@PathVariable Long issuerId) {
        CertificateAuthority issuer = caRepo.findById(issuerId)
                .orElseThrow(() -> new RuntimeException("Issuer not found"));
        return tplRepo.findByIssuerCa(issuer);
    }
}
