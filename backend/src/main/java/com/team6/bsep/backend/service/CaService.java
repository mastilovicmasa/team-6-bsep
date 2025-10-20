package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CaUserResponse;
import com.team6.bsep.backend.dto.CreateCaUserRequest;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.model.UserStatus;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.team6.bsep.backend.model.CertificateAuthority;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.security.SecureRandom;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaService {

    private final CertificateAuthorityRepository caRepo;
    private final UserRepository userRepo;
    private final CryptoService cryptoService;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

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
    public CertificateAuthority issueCaCertificateForUser(String email, String subjectDn, int pathLenConstraint) throws Exception {

        System.out.println("=== [START] Issue CA certificate for user " + email + " ===");

        // 🧩 1. Pronađi Root CA
        CertificateAuthority rootCa = caRepo.findByRootTrue()
                .orElseThrow(() -> new IllegalStateException("Root CA not found in database"));

        // 👤 2. Nađi korisnika
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("CA user not found: " + email));

        // 🏷️ 3. Definiši alias
        String alias = email + "-ca";

        // 🪄 4. Pozovi zajedničku funkciju
        CertificateAuthority caEntity = issueCaInternal(rootCa, subjectDn, alias, pathLenConstraint, user);

        System.out.println("=== [END] Successfully issued CA certificate for user " + email + " ===");
        return caEntity;
    }


    private static String generateRandomSecret() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Transactional
    public CertificateAuthority issueSubCaCertificateForUser(String issuerEmail, String targetEmail, String subjectDn) throws Exception {

        System.out.println("=== [START] SubCA issue from " + issuerEmail + " to " + targetEmail + " ===");

        // 👤 1. Issuer (CA koji potpisuje)
        User issuerUser = userRepo.findByEmail(issuerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Issuer CA user not found: " + issuerEmail));
        CertificateAuthority issuerCa = issuerUser.getCertificateAuthority();
        if (issuerCa == null)
            throw new IllegalStateException("User is not associated with any CA certificate.");

        if (issuerCa.getPathLenConstraint() <= 0)
            throw new IllegalStateException("This CA cannot issue further CA certificates (pathLenConstraint=0).");

        // 🎯 2. Target korisnik
        User targetUser = userRepo.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found: " + targetEmail));

        // 🪄 3. Poziv zajedničke funkcije
        int newPathLen = issuerCa.getPathLenConstraint() - 1;
        String alias = targetEmail + "-ca";
        CertificateAuthority caEntity = issueCaInternal(issuerCa, subjectDn, alias, newPathLen, targetUser);

        System.out.println("=== [END] Subordinate CA issued successfully to " + targetEmail + " ===");
        return caEntity;
    }

    private CertificateAuthority issueCaInternal(
            CertificateAuthority issuerCa,
            String subjectDn,
            String alias,
            int pathLenConstraint,
            User targetUser
    ) throws Exception {

        // 🔓 1. Dešifruj lozinku keystore-a izdavaoca (root ili intermediate)
        String issuerPass = cryptoService.decrypt(issuerCa.getKeystorePasswordEnc());

        // 📂 2. Učitaj issuer keystore
        KeyStore issuerKs = KeyStore.getInstance("PKCS12");
        try (var in = Files.newInputStream(Path.of(issuerCa.getKeystorePath()))) {
            issuerKs.load(in, issuerPass.toCharArray());
        }

        // 🔑 3. Izvuci issuer privatni ključ i sertifikat
        PrivateKey issuerKey = (PrivateKey) issuerKs.getKey(issuerCa.getKeystoreAlias(), issuerPass.toCharArray());
        X509Certificate issuerCert = (X509Certificate) issuerKs.getCertificate(issuerCa.getKeystoreAlias());

        // 🔐 4. Generiši novu lozinku za novi keystore
        String ksPassword = generateRandomSecret();

        // 🏗️ 5. Kreiraj novi CA keystore potpisan od izdavaoca
        KeyStore newKs = cryptoService.createCaKeystore(issuerCert, issuerKey, subjectDn, alias, ksPassword, pathLenConstraint);

        // 💾 6. Snimi novi keystore fajl
        Path ksPath = Path.of("data/ca-users/" + alias + ".p12");
        Files.createDirectories(ksPath.getParent());
        try (OutputStream os = Files.newOutputStream(ksPath)) {
            newKs.store(os, ksPassword.toCharArray());
        }

        // 🔍 7. Verifikuj novi sertifikat
        X509Certificate newCert = (X509Certificate) newKs.getCertificate(alias);
        newCert.verify(issuerCert.getPublicKey());

        // 🧱 8. Kreiraj novi CA entitet
        CertificateAuthority caEntity = CertificateAuthority.builder()
                .root(false)
                .subjectDn(subjectDn)
                .serialHex(newCert.getSerialNumber().toString(16))
                .notBefore(newCert.getNotBefore().toInstant())
                .notAfter(newCert.getNotAfter().toInstant())
                .pathLenConstraint(pathLenConstraint)
                .keystorePath(ksPath.toString())
                .keystoreAlias(alias)
                .keystorePasswordEnc(cryptoService.encrypt(ksPassword))
                .keyPasswordEnc(cryptoService.encrypt(ksPassword))
                .issuer(issuerCa)
                .build();

        // 💿 9. Sačuvaj sve u bazi i poveži korisnika
        caRepo.save(caEntity);
        targetUser.setCertificateAuthority(caEntity);
        userRepo.save(targetUser);

        System.out.println("✅ Issued CA cert: " + targetUser.getEmail() +
                " (issuer=" + issuerCa.getSubjectDn() + ", pathLen=" + pathLenConstraint + ")");

        return caEntity;
    }



    @Transactional
    public void createSubCaUser(String issuerEmail, CreateCaUserRequest req) {
        String normalized = req.email().trim().toLowerCase();

        if (userRepo.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 1️⃣ Pronađi issuer CA korisnika
        User issuerUser = userRepo.findByEmail(issuerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issuer CA user not found"));
        CertificateAuthority issuerCa = issuerUser.getCertificateAuthority();

        if (issuerCa == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issuer is not associated with a CA certificate");

        if (issuerCa.getPathLenConstraint() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This CA cannot create subordinate users (pathLenConstraint=0)");

        // 2️⃣ Generiši login lozinku
        String rawPassword = UUID.randomUUID().toString().substring(0, 12);

        // 3️⃣ Kreiraj user entitet sa CA rolom
        var newUser = User.builder()
                .email(normalized)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .organization(req.organization())
                .passwordHash(encoder.encode(rawPassword))
                .role(UserRole.CA)
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .activatedAt(Instant.now())
                .issuerCa(issuerCa)
                .build();

        userRepo.save(newUser);

        // 4️⃣ Pošalji mejl novom korisniku
        emailService.sendCaUserCreated(normalized, rawPassword, req.firstName());

        // 5️⃣ Log info
        System.out.printf(
                "✅ Subordinate CA user created by %s (issuer CA=%s, pathLen=%d): %s%n",
                issuerEmail,
                issuerCa.getSubjectDn(),
                issuerCa.getPathLenConstraint(),
                normalized
        );
    }

    @Transactional(readOnly = true)
    public List<User> getSubordinateCaUsers(String issuerEmail) {
        User issuerUser = userRepo.findByEmail(issuerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issuer user not found"));
        CertificateAuthority issuerCa = issuerUser.getCertificateAuthority();

        if (issuerCa == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with a CA certificate");
        }

        List<User> subordinates = userRepo.findAllByIssuerCa(issuerCa);
        System.out.printf("ℹ️ Found %d subordinate CA users for issuer %s (%s)%n",
                subordinates.size(), issuerEmail, issuerCa.getSubjectDn());

        return subordinates;
    }



}
