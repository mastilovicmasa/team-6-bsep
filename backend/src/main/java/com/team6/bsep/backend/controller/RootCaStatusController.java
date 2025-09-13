package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.service.CryptoService;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RootCaStatusController {

    private final CertificateAuthorityRepository caRepo;
    private final CryptoService crypto;

    @GetMapping("/api/dev/ca/root/status")
    public Map<String, Object> rootStatus() throws Exception {
        CertificateAuthority ca = caRepo.findAll().stream()
                .filter(CertificateAuthority::isRoot)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Root CA not found in DB"));

        Path ksPath = Path.of(ca.getKeystorePath());
        boolean fileExists = Files.exists(ksPath);

        String ksPass = crypto.decrypt(ca.getKeystorePasswordEnc());
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (var in = Files.newInputStream(ksPath)) {
            ks.load(in, ksPass.toCharArray());
        }

        X509Certificate cert = (X509Certificate) ks.getCertificate(ca.getKeystoreAlias());
        cert.checkValidity(new Date()); // važi danas
        boolean selfSigned = cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal());
        cert.verify(cert.getPublicKey()); // potpis validan (self-signed)

        int basicConstraints = cert.getBasicConstraints(); // >=0 ⇒ CA
        boolean isCa = basicConstraints >= 0;

        boolean keyCertSign = false, cRLSign = false;
        boolean[] ku = cert.getKeyUsage();
        if (ku != null && ku.length > 6) {
            keyCertSign = ku[5];
            cRLSign = ku[6];
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("dbSubjectDn", ca.getSubjectDn());
        out.put("keystorePath", ca.getKeystorePath());
        out.put("fileExists", fileExists);
        out.put("alias", ca.getKeystoreAlias());
        out.put("serialHex(db)", ca.getSerialHex());
        out.put("subject(cert)", cert.getSubjectX500Principal().getName());
        out.put("issuer(cert)", cert.getIssuerX500Principal().getName());
        out.put("notBefore", cert.getNotBefore().toInstant());
        out.put("notAfter", cert.getNotAfter().toInstant());
        out.put("nowInRange",
                Instant.now().isBefore(cert.getNotAfter().toInstant())
                        && Instant.now().isAfter(cert.getNotBefore().toInstant()));
        out.put("signatureAlg", cert.getSigAlgName());
        out.put("isSelfSigned", selfSigned);
        out.put("isCA", isCa);
        out.put("pathLenConstraint", basicConstraints >= 0 ? basicConstraints : null);
        out.put("keyUsage.keyCertSign", keyCertSign);
        out.put("keyUsage.cRLSign", cRLSign);
        return out;
    }
}