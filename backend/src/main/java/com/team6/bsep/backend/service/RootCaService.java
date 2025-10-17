package com.team6.bsep.backend.service;


import com.team6.bsep.backend.service.CryptoService;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class RootCaService {

    static { Security.addProvider(new BouncyCastleProvider()); }

    private final CertificateAuthorityRepository caRepo;
    private final CryptoService crypto;

    @Value("${app.ca.root.subject}")       String subject;
    @Value("${app.ca.root.key-size}")      int keySize;
    @Value("${app.ca.root.validity-days}") long validityDays;
    @Value("${app.ca.root.sig-alg}")       String sigAlg;
    @Value("${app.ca.root.path-len}")      int pathLen;
    @Value("${app.ca.root.keystore-path}") String ksPath;
    @Value("${app.ca.root.keystore-alias}")String ksAlias;

    @Transactional

        public void createRootIfMissing() throws Exception {
            if (caRepo.existsByRootTrue()) return;
        var kpg = KeyPairGenerator.getInstance("RSA"); kpg.initialize(keySize);
        var kp = kpg.generateKeyPair();

        var now = Instant.now();
        var notbefore = Date.from(now.minus(1, ChronoUnit.DAYS));
        var notafter = Date.from(now.plus(validityDays, ChronoUnit.DAYS));
        var dn = new X500Name(subject);
        var serial = new BigInteger(160, SecureRandom.getInstanceStrong());

        var builder = new JcaX509v3CertificateBuilder(dn, serial, notbefore, notafter, dn, kp.getPublic());
        var ext = new JcaX509ExtensionUtils();
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(pathLen));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        builder.addExtension(Extension.subjectKeyIdentifier, false, ext.createSubjectKeyIdentifier(kp.getPublic()));
        builder.addExtension(Extension.authorityKeyIdentifier, false, ext.createAuthorityKeyIdentifier(kp.getPublic()));

        var signer = new JcaContentSignerBuilder(sigAlg).build(kp.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(builder.build(signer));
        cert.verify(kp.getPublic());

        var ksPassword = randomSecret();
        var ks = KeyStore.getInstance("PKCS12"); ks.load(null, null);
        ks.setKeyEntry(ksAlias, kp.getPrivate(), ksPassword.toCharArray(), new java.security.cert.Certificate[]{cert});
        var p = Path.of(ksPath); Files.createDirectories(p.getParent());
        try (OutputStream os = Files.newOutputStream(p)) { ks.store(os, ksPassword.toCharArray()); }

        var ca = CertificateAuthority.builder()
                .root(true)
                .subjectDn(subject)
                .serialHex(serial.toString(16))
                .notBefore(notbefore.toInstant())
                .notAfter(notafter.toInstant())
                .pathLenConstraint(pathLen)
                .keystorePath(p.toString())
                .keystoreAlias(ksAlias)
                .keystorePasswordEnc(crypto.encrypt(ksPassword))
                .keyPasswordEnc(crypto.encrypt(ksPassword))
                .issuer(null)
                .build();
        caRepo.save(ca);

        System.out.println("=== ROOT CA CREATED === " + subject + " @ " + p);
    }

    private static String randomSecret() {
        byte[] bytes = new byte[24]; new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
