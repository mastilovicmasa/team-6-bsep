package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CaUserResponse;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.team6.bsep.backend.model.CertificateAuthority;

import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.security.SecureRandom;


import java.util.List;

@Service
@RequiredArgsConstructor
public class CaService {

    private final CertificateAuthorityRepository caRepo;
    private final UserRepository userRepo;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public List<CaUserResponse> getAllCaUsers() {
        List<User> caUsers = userRepo.findByRole(UserRole.CA);

        return caUsers.stream()
                .map(user -> new CaUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getOrganization(),
                        user.getCertificateAuthority() != null // ako ima CA sertifikat
                ))
                .toList();
    }

    @Transactional
    public CertificateAuthority issueCaCertificateForUser(String email, String subjectDn) throws Exception {

        System.out.println("=== [START] Issue CA certificate for user " + email + " ===");

        // 1️⃣ Pronađi Root CA
        var rootCa = caRepo.findByRootTrue()
                .orElseThrow(() -> new IllegalStateException("Root CA not found in database"));
        System.out.println("Root CA found in DB:");
        System.out.println("  subjectDn: " + rootCa.getSubjectDn());
        System.out.println("  path:      " + rootCa.getKeystorePath());
        System.out.println("  alias:     " + rootCa.getKeystoreAlias());

        // 2️⃣ Učitaj root keystore
        String rootPassword = cryptoService.decrypt(rootCa.getKeystorePasswordEnc());
        KeyStore rootKs = KeyStore.getInstance("PKCS12");
        try (var in = Files.newInputStream(Path.of(rootCa.getKeystorePath()))) {
            rootKs.load(in, rootPassword.toCharArray());
        }
        PrivateKey rootPrivateKey = (PrivateKey) rootKs.getKey(rootCa.getKeystoreAlias(), rootPassword.toCharArray());
        X509Certificate rootCert = (X509Certificate) rootKs.getCertificate(rootCa.getKeystoreAlias());

        System.out.println("Root cert details:");
        System.out.println("  Subject: " + rootCert.getSubjectX500Principal());
        System.out.println("  Issuer : " + rootCert.getIssuerX500Principal());
        System.out.println("  Valid  : " + rootCert.getNotBefore() + " - " + rootCert.getNotAfter());
        System.out.println("  isSelfSigned: " + rootCert.getSubjectX500Principal().equals(rootCert.getIssuerX500Principal()));

        // 3️⃣ Nađi korisnika kome se izdaje CA sertifikat
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("CA user not found: " + email));
        System.out.println("Target user found: " + user.getEmail() + " (" + user.getOrganization() + ")");

        // 4️⃣ Generiši random lozinku za novi keystore
        String ksPassword = generateRandomSecret();
        String alias = email + "-ca";
        System.out.println("Generated new keystore alias: " + alias);

        // 5️⃣ Kreiraj CA keystore potpisan od root-a
        System.out.println("Creating new CA keystore...");
        KeyStore ks = cryptoService.createCaKeystore(rootCert, rootPrivateKey, subjectDn, alias, ksPassword);
        System.out.println("New CA keystore created successfully.");

        // 6️⃣ Snimi keystore fajl
        Path ksPath = Path.of("data/ca-users/" + alias + ".p12");
        Files.createDirectories(ksPath.getParent());
        try (OutputStream os = Files.newOutputStream(ksPath)) {
            ks.store(os, ksPassword.toCharArray());
        }
        System.out.println("Keystore file saved: " + ksPath.toAbsolutePath());

        // 7️⃣ Izvuci sertifikat iz keystorea
        X509Certificate newCert = (X509Certificate) ks.getCertificate(alias);

        System.out.println("New cert details:");
        System.out.println("  Subject: " + newCert.getSubjectX500Principal());
        System.out.println("  Issuer : " + newCert.getIssuerX500Principal());
        System.out.println("  Valid  : " + newCert.getNotBefore() + " - " + newCert.getNotAfter());
        System.out.println("  Serial : " + newCert.getSerialNumber().toString(16));

        try {
            newCert.verify(rootCert.getPublicKey());
            System.out.println("✅ Certificate successfully verified against Root public key.");
        } catch (Exception e) {
            System.out.println("❌ Certificate verification FAILED: " + e.getMessage());
            e.printStackTrace();
        }

        // 8️⃣ Kreiraj CA entitet i sačuvaj u bazi
        var caEntity = CertificateAuthority.builder()
                .root(false)
                .subjectDn(subjectDn)
                .serialHex(newCert.getSerialNumber().toString(16))
                .notBefore(newCert.getNotBefore().toInstant())
                .notAfter(newCert.getNotAfter().toInstant())
                .pathLenConstraint(0)
                .keystorePath(ksPath.toString())
                .keystoreAlias(alias)
                .keystorePasswordEnc(cryptoService.encrypt(ksPassword))
                .keyPasswordEnc(cryptoService.encrypt(ksPassword))
                // .issuer(rootCa) // ako još nemaš ovo polje, komentariši
                .build();

        caRepo.save(caEntity);
        System.out.println("Saved CertificateAuthority entity to DB (id=" + caEntity.getId() + ")");

        // 9️⃣ Poveži CA sa korisnikom
        user.setCertificateAuthority(caEntity);
        userRepo.save(user);
        System.out.println("Linked new CA to user: " + user.getEmail());

        System.out.println("=== [END] Successfully issued CA certificate for user " + email + " ===");
        return caEntity;
    }


    private static String generateRandomSecret() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


}
