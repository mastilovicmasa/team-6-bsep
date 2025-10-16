package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CertificateRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
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


    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public X500Name createX500Name(CertificateRequest dto) {
        return new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, dto.getCommonName())
                .addRDN(BCStyle.O, dto.getOrganization())
                .addRDN(BCStyle.OU, dto.getOrganizationalUnit())
                .addRDN(BCStyle.C, dto.getCountry())
                .addRDN(BCStyle.EmailAddress, dto.getEmail())
                .build();
    }

    public X509Certificate generateCertificate(X500Name subject, X500Name issuer,
                                               PublicKey subjectPublicKey, PrivateKey issuerPrivateKey,
                                               int validityDays, boolean isCA) throws Exception {
        Date notBefore = new Date();
        Date notAfter = Date.from(Instant.now().plus(validityDays, ChronoUnit.DAYS));
        BigInteger serial = new BigInteger(64, new SecureRandom());

        var builder = new JcaX509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, subjectPublicKey
        );
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCA));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerPrivateKey);
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    public KeyStore loadKeyStore(String path, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(fis, password.toCharArray());
            return ks;
        }
    }

    public PrivateKey getPrivateKey(KeyStore ks, String alias, String password) throws Exception {
        return (PrivateKey) ks.getKey(alias, password.toCharArray());
    }

    public X509Certificate getCertificate(KeyStore ks, String alias) throws Exception {
        return (X509Certificate) ks.getCertificate(alias);
    }

    public void saveToKeystore(String path, String alias, PrivateKey key,
                               X509Certificate cert, X509Certificate issuerCert, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        Certificate[] chain = new Certificate[]{cert, issuerCert};
        ks.setKeyEntry(alias, key, password.toCharArray(), chain);

        File file = new File(path);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ks.store(fos, password.toCharArray());
        }
    }


}
