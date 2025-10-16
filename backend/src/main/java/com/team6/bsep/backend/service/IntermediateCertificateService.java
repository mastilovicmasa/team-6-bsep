package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CertificateRequest;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import jakarta.transaction.Transactional;
import org.bouncycastle.asn1.x500.X500Name;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class IntermediateCertificateService {

    private final CertificateAuthorityRepository caRepo;
    private final CryptoService crypto;

    public IntermediateCertificateService(CertificateAuthorityRepository caRepo, CryptoService crypto) {
        this.caRepo = caRepo;
        this.crypto = crypto;
    }

    /**
     * Izdaje novi intermediate CA sertifikat, potpisan root CA sertifikatom.
     */
    @Transactional
    public CertificateAuthority issueIntermediateCertificate(CertificateRequest dto) {
        try {
            // 1️⃣ Pronađi root CA (issuer)
            CertificateAuthority issuerCA = caRepo.findByRootTrue()
                    .orElseThrow(() -> new RuntimeException("Root CA not found"));

            // 2️⃣ Učitaj keystore root CA
            String issuerPass = crypto.decrypt(issuerCA.getKeystorePasswordEnc());
            KeyStore issuerKs = crypto.loadKeyStore(issuerCA.getKeystorePath(), issuerPass);
            PrivateKey issuerPrivateKey = crypto.getPrivateKey(issuerKs, issuerCA.getKeystoreAlias(), issuerPass);
            X509Certificate issuerCert = crypto.getCertificate(issuerKs, issuerCA.getKeystoreAlias());

            // 3️⃣ Generiši novi par ključeva
            KeyPair newKeyPair = crypto.generateKeyPair();

            // 4️⃣ Formiraj X500Name subjekta na osnovu unetih podataka
            X500Name subject = crypto.createX500Name(dto);

            // 5️⃣ Generiši novi sertifikat (CA → Intermediate)
            X509Certificate newCert = crypto.generateCertificate(
                    subject,
                    new X500Name(issuerCert.getSubjectX500Principal().getName()),
                    newKeyPair.getPublic(),
                    issuerPrivateKey,
                    dto.getValidityInDays(),
                    true // isCA = true
            );

            // 6️⃣ Sačuvaj novi CA u bazi
            String alias = "intermediate-" + UUID.randomUUID();
            String path = "data/keystores/" + alias + ".p12";
            String randomPassword = UUID.randomUUID().toString();

            CertificateAuthority newCA = CertificateAuthority.builder()
                    .root(false)
                    .subjectDn(newCert.getSubjectX500Principal().getName())
                    .serialHex(newCert.getSerialNumber().toString(16))
                    .notBefore(newCert.getNotBefore().toInstant())
                    .notAfter(newCert.getNotAfter().toInstant())
                    .keystoreAlias(alias)
                    .keystorePath(path)
                    .keystorePasswordEnc(crypto.encrypt(randomPassword))
                    .keyPasswordEnc(crypto.encrypt(randomPassword))
                    .revokedAt(null)
                    .build();

            caRepo.save(newCA);

            try {
                newCert.verify(issuerCert.getPublicKey());
                System.out.println("✅ Certificate is correctly signed by issuer");
            } catch (Exception e) {
                System.err.println("❌ Verification failed: " + e.getMessage());
            }


            System.out.println("Issuer: " + issuerCert.getSubjectX500Principal());
            System.out.println("NewCert Issuer: " + newCert.getIssuerX500Principal());
            System.out.println("NewCert Subject: " + newCert.getSubjectX500Principal());
            System.out.println(crypto.decrypt("17107"));

            newCert.verify(issuerCert.getPublicKey());

            // 7️⃣ Kreiraj i snimi novi keystore (sertifikat + root lanac)
            crypto.saveToKeystore(
                    path,
                    alias,
                    newKeyPair.getPrivate(),
                    newCert,
                    issuerCert,
                    randomPassword
            );

            System.out.println("=== Intermediate CA created: " + newCA.getSubjectDn() + " ===");
            return newCA;

        } catch (Exception e) {
            throw new RuntimeException("Failed to issue intermediate certificate: " + e.getMessage(), e);
        }
    }
}
