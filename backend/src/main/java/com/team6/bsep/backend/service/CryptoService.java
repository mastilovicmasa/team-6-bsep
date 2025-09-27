package com.team6.bsep.backend.service;

import org.bouncycastle.asn1.x500.X500Name;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
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

}
