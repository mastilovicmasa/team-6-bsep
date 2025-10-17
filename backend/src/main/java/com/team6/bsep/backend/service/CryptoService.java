package com.team6.bsep.backend.service;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import com.team6.bsep.backend.utils.SelfSignedCertGenerator;


@Service
public class CryptoService {
    @Value("${app.crypto.kek}") String kek;
    private final SecureRandom rng = new SecureRandom();

    public String encrypt(String plain){
        try{
            byte[] salt=new byte[16], iv=new byte[12]; rng.nextBytes(salt); rng.nextBytes(iv);
            var key = derive(kek.toCharArray(), salt);
            var c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            var ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(28+ct.length).put(salt).put(iv).put(ct).array());
        }catch(Exception e){ throw new RuntimeException(e); }
    }
    public String decrypt(String enc){
        try{
            var bb = ByteBuffer.wrap(Base64.getDecoder().decode(enc));
            byte[] salt=new byte[16], iv=new byte[12]; bb.get(salt); bb.get(iv);
            byte[] ct=new byte[bb.remaining()]; bb.get(ct);
            var key = derive(kek.toCharArray(), salt);
            var c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        }catch(Exception e){ throw new RuntimeException(e); }
    }
    private static SecretKey derive(char[] pwd, byte[] salt) throws Exception{
        var spec = new PBEKeySpec(pwd, salt, 200_000, 256);
        var skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(skf.generateSecret(spec).getEncoded(), "AES");
    }

    public void createKeystore(String path, String alias, String password) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        X500Name owner = new X500Name("CN=" + alias);
        X509Certificate cert = SelfSignedCertGenerator.generate(owner, kp);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, password.toCharArray());
        ks.setKeyEntry(alias, kp.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});

        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(path)) {
            ks.store(fos, password.toCharArray());
        }
        System.out.println("Keystore created at: " + file.getAbsolutePath());

    }

    public KeyStore createCaKeystore(X509Certificate issuerCert, PrivateKey issuerKey,
                                     String subjectDn, String alias, String password,
                                     int pathLenConstraint) throws Exception {

        System.out.println("=== [CryptoService] Starting createCaKeystore ===");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        if (issuerCert == null) throw new IllegalArgumentException("Issuer certificate is null!");
        if (issuerKey == null) throw new IllegalArgumentException("Issuer private key is null!");

        System.out.println("Issuer certificate: " + issuerCert.getSubjectX500Principal());
        System.out.println("Subject DN for new cert: " + subjectDn);
        System.out.println("PathLenConstraint for new CA: " + pathLenConstraint);

        // 1️⃣ Generate key pair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        System.out.println("Key pair generated for new CA.");

        // 2️⃣ Define validity
        Instant now = Instant.now();
        Date notBefore = Date.from(now.minus(1, ChronoUnit.DAYS));
        Date notAfter = Date.from(now.plus(3650, ChronoUnit.DAYS)); // 10 years

        X500Name issuer = new X500Name(issuerCert.getSubjectX500Principal().getName());
        X500Name subject = new X500Name(subjectDn);
        BigInteger serial = new BigInteger(160, new SecureRandom());
        System.out.println("Serial: " + serial.toString(16));

        // 3️⃣ Build certificate
        var builder = new JcaX509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, kp.getPublic());

        var extUtils = new JcaX509ExtensionUtils();

        // ✅ BasicConstraints: CA=true i setuj pathLenConstraint
        builder.addExtension(
                Extension.basicConstraints,
                true,
                new BasicConstraints(pathLenConstraint)
        );

        // ✅ KeyUsage: omogućava potpisivanje drugih sertifikata i CRL-ova
        builder.addExtension(
                Extension.keyUsage,
                true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign)
        );

        // ✅ Identifikatori
        builder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(kp.getPublic())
        );
        builder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(issuerCert.getPublicKey())
        );

        // 4️⃣ Sign with issuer private key
        System.out.println("Preparing to sign certificate...");
        ContentSigner signer;
        try {
            signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC")
                    .build(issuerKey);
        } catch (Exception e) {
            System.out.println("❌ Failed to create ContentSigner: " + e.getMessage());
            throw e;
        }

        System.out.println("Signer successfully created, now building cert...");
        X509Certificate newCert;
        try {
            newCert = new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(builder.build(signer));
            System.out.println("Certificate built successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to build certificate: " + e.getMessage());
            throw e;
        }

        // 5️⃣ Verify
        try {
            newCert.verify(issuerCert.getPublicKey());
            System.out.println("✅ Certificate verified successfully against issuer public key.");
        } catch (Exception e) {
            System.out.println("❌ Verification failed: " + e.getMessage());
            throw e;
        }

        // 6️⃣ Create keystore and insert chain
        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        ks.load(null, null);
        ks.setKeyEntry(
                alias,
                kp.getPrivate(),
                password.toCharArray(),
                new java.security.cert.Certificate[]{newCert, issuerCert}
        );
        System.out.println("Keystore entry created successfully for alias: " + alias);

        System.out.println("=== [CryptoService] Finished createCaKeystore ===");
        return ks;
    }



}
