package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CertificateRequest;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.UserRepository;
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
    private final UserRepository userRepo;

    public IntermediateCertificateService(CertificateAuthorityRepository caRepo, CryptoService crypto, UserRepository userRepo) {
        this.caRepo = caRepo;
        this.crypto = crypto;
        this.userRepo = userRepo;
    }

    /**
     * Izdaje novi intermediate CA sertifikat, potpisan root CA sertifikatom.
     */
    @Transactional
    public CertificateAuthority issueIntermediateCertificate(CertificateRequest dto, String issuerEmail) {
        try {
            // 🔹 1️⃣ Pronađi korisnika koji izdaje (može biti root admin ili CA korisnik)
            User issuerUser = userRepo.findByEmail(issuerEmail)
                    .orElseThrow(() -> new RuntimeException("Issuer user not found: " + issuerEmail));

            // 🔹 2️⃣ Odredi koji CA se koristi kao izdavalac (root ili korisnikov CA)
            CertificateAuthority issuerCA;
            if (issuerUser.getRole() == UserRole.ADMIN) {
                issuerCA = caRepo.findByRootTrue()
                        .orElseThrow(() -> new RuntimeException("Root CA not found"));
            } else {
                issuerCA = issuerUser.getCertificateAuthority();
                if (issuerCA == null) {
                    throw new RuntimeException("User has no linked CA");
                }
            }

            // 🔹 3️⃣ Proveri da li CA sme da izdaje intermediate sertifikate
            if (issuerCA.getPathLenConstraint() != null && issuerCA.getPathLenConstraint() <= 0) {
                throw new RuntimeException("This CA cannot issue further intermediate certificates (pathLenConstraint=0)");
            }

            // 🔹 4️⃣ Učitaj keystore CA koji izdaje
            String issuerPass = crypto.decrypt(issuerCA.getKeystorePasswordEnc());
            KeyStore issuerKs = crypto.loadKeyStore(issuerCA.getKeystorePath(), issuerPass);
            PrivateKey issuerPrivateKey = crypto.getPrivateKey(issuerKs, issuerCA.getKeystoreAlias(), issuerPass);
            X509Certificate issuerCert = crypto.getCertificate(issuerKs, issuerCA.getKeystoreAlias());

            // 🔹 5️⃣ Generiši novi par ključeva za novi intermediate CA
            KeyPair newKeyPair = crypto.generateKeyPair();

            // 🔹 6️⃣ Formiraj X500Name subjekta (podatke o novom CA)
            X500Name subject = crypto.createX500Name(dto); // npr. CN=Org Lab CA, O=Org, C=RS

            // 🔹 7️⃣ Generiši novi sertifikat (potpisan od issuer-a)
            X509Certificate newCert = crypto.generateCertificate(
                    subject,
                    new X500Name(issuerCert.getSubjectX500Principal().getName()),
                    newKeyPair.getPublic(),
                    issuerPrivateKey,
                    dto.getValidityInDays(),
                    true // isCA = true
            );

            // 🔹 8️⃣ Proveri potpis
            newCert.verify(issuerCert.getPublicKey());
            System.out.println("✅ Intermediate certificate correctly signed by " + issuerCert.getSubjectX500Principal());

            // 🔹 9️⃣ Pripremi keystore i metapodatke za novi CA
            String alias = "intermediate-" + UUID.randomUUID();
            String path = "data/keystores/" + alias + ".p12";
            String randomPassword = UUID.randomUUID().toString();

            // 🔹 10️⃣ Sačuvaj novi CA u bazi
            CertificateAuthority newCA = CertificateAuthority.builder()
                    .root(false)
                    .subjectDn(newCert.getSubjectX500Principal().getName())
                    .serialHex(newCert.getSerialNumber().toString(16))
                    .notBefore(newCert.getNotBefore().toInstant())
                    .notAfter(newCert.getNotAfter().toInstant())
                    .pathLenConstraint(0) // novi CA po defaultu ne može dalje izdavati (možeš podesiti iz dto-a)
                    .keystoreAlias(alias)
                    .keystorePath(path)
                    .keystorePasswordEnc(crypto.encrypt(randomPassword))
                    .keyPasswordEnc(crypto.encrypt(randomPassword))
                    .issuer(issuerCA) // 🔗 poveži sa CA koji ga je izdao
                    .revokedAt(null)
                    .build();

            caRepo.save(newCA);

            // 🔹 11️⃣ Sačuvaj novi keystore (sertifikat + lanac)
            crypto.saveToKeystore(
                    path,
                    alias,
                    newKeyPair.getPrivate(),
                    newCert,
                    issuerCert, // dodaj issuer u lanac
                    randomPassword
            );

            System.out.println("=== Intermediate CA created: " + newCA.getSubjectDn() + " ===");
            return newCA;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to issue intermediate certificate: " + e.getMessage(), e);
        }
    }

}
