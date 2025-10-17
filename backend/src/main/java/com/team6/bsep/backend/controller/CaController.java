package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.CaInfo;
import com.team6.bsep.backend.dto.CaUserResponse;
import com.team6.bsep.backend.dto.CreateCaUserRequest;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.service.CaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ca")
@RequiredArgsConstructor
public class CaController {

    private final CertificateAuthorityRepository caRepo;
    private final CaService caService;

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

    @PostMapping("/create-subordinate")
    @PreAuthorize("hasRole('CA_USER')")
    public ResponseEntity<?> addCaUser(@RequestBody CreateCaUserRequest req) {
        try {
            String issuerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            caService.createSubCaUser(issuerEmail, req);
            return ResponseEntity.ok(Map.of("message", "Subordinate CA user created successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/issue-subca/{email}")
    @PreAuthorize("hasRole('CA_USER')")
    public ResponseEntity<?> issueSubCaCertificate(
            @PathVariable String email,
            @RequestBody Map<String, String> body) {
        try {
            String subjectDn = body.get("subjectDn");

            // Dobavi email ulogovanog CA korisnika iz SecurityContext-a
            String issuerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            caService.issueSubCaCertificateForUser(issuerEmail, email, subjectDn);

            return ResponseEntity.ok(Map.of("message", "Subordinate CA certificate issued successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/subordinates")
    @PreAuthorize("hasRole('CA_USER')")
    public ResponseEntity<?> getSubordinateUsers() {
        try {
            String issuerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            List<User> subordinates = caService.getSubordinateCaUsers(issuerEmail);

            List<CaUserResponse> dto = subordinates.stream()
                    .map(u -> {
                        CaUserResponse resp = new CaUserResponse();
                        resp.setId(u.getId());
                        resp.setEmail(u.getEmail());
                        resp.setFirstName(u.getFirstName());
                        resp.setLastName(u.getLastName());
                        resp.setOrganization(u.getOrganization());
                        resp.setHasCaCertificate(u.getCertificateAuthority() != null);
                        return resp;
                    })
                    .toList();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }



}

