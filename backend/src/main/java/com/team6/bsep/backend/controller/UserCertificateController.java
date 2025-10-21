package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.repository.EndEntityCertificateRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCertificateController {

    private final UserRepository userRepo;
    private final EndEntityCertificateRepository eeCertRepo;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKeyForCurrentUser() throws Exception {
        var user = getCurrentUser();

        var certEntity = eeCertRepo.findByCsr_User_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        // Pretvori PEM string u X509Certificate
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        var cert = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(certEntity.getPem().getBytes(StandardCharsets.UTF_8))
        );

        // Izvuci javni ključ i konvertuj u PEM format
        byte[] encoded = cert.getPublicKey().getEncoded();
        StringWriter writer = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(writer)) {
            pemWriter.writeObject(new PemObject("PUBLIC KEY", encoded));
        }

        return ResponseEntity.ok(writer.toString());
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails userDetails)
                ? userDetails.getUsername()
                : principal.toString();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

}
