package com.team6.bsep.backend.service;

import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.RevokedCertificate;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.RevokedCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for certificate revocation and CRL generation.
 * Compatible with existing CryptoService and CA structure.
 */
@Service
@RequiredArgsConstructor
public class RevocationService {

    private final RevokedCertificateRepository revokedRepo;
    private final CertificateAuthorityRepository caRepo;
    private final CryptoService cryptoService;

    /**
     * Revokes a certificate by serial number and stores it in the revoked list.
     * Optionally updates the CRL for the issuer CA.
     */
    public void revokeCertificate(String serialNumber, String reason) throws Exception {
        if (revokedRepo.existsBySerialNumber(serialNumber)) {
            throw new IllegalStateException("Certificate already revoked");
        }

        // Find the certificate in CA repo
        CertificateAuthority cert = caRepo.findBySerialHex(serialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found with serial: " + serialNumber));

        // Save revocation record
        revokedRepo.save(new RevokedCertificate(serialNumber, reason));

        // Update CRL for the issuer CA (if exists)
        CertificateAuthority issuer = cert.getIssuer();
        if (issuer != null) {
            generateAndSaveCRL(issuer);
        }
    }

    /**
     * Checks if a certificate has been revoked.
     */
    public boolean isRevoked(String serialNumber) {
        return revokedRepo.existsBySerialNumber(serialNumber);
    }

    /**
     * Generates a CRL signed by the given CA and saves it as a .crl file.
     */
    public X509CRL generateAndSaveCRL(CertificateAuthority issuerCa) throws Exception {
        // Decrypt and load keystore info
        String issuerPass = cryptoService.decrypt(issuerCa.getKeystorePasswordEnc());
        PrivateKey issuerKey = cryptoService.loadPrivateKeyFromKeystore(
                issuerCa.getKeystorePath(),
                issuerCa.getKeystoreAlias(),
                issuerPass
        );
        X509Certificate issuerCert = cryptoService.loadCertificateFromKeystore(
                issuerCa.getKeystorePath(),
                issuerCa.getKeystoreAlias(),
                issuerPass
        );

        // Prepare CRL builder
        var builder = new X509v2CRLBuilder(
                new org.bouncycastle.asn1.x500.X500Name(issuerCert.getSubjectX500Principal().getName()),
                new Date()
        );

        // Add revoked entries
        List<RevokedCertificate> revokedList = revokedRepo.findAll();
        for (RevokedCertificate rc : revokedList) {
            builder.addCRLEntry(
                    new BigInteger(rc.getSerialNumber(), 16),
                    Date.from(rc.getRevokedAt().atZone(ZoneId.systemDefault()).toInstant()),
                    0
            );
        }

        // Add CRL distribution point (optional)
        GeneralName dpName = new GeneralName(GeneralName.uniformResourceIdentifier,
                "http://localhost:8080/api/admin/revoke/crl");
        GeneralNames gns = new GeneralNames(dpName);
        builder.addExtension(Extension.cRLDistributionPoints, false, gns);

        // Sign CRL
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey);
        X509CRLHolder holder = builder.build(signer);
        X509CRL crl = new JcaX509CRLConverter().getCRL(holder);

        // Save CRL file
        String crlPath = "data/crl/" + issuerCa.getKeystoreAlias() + ".crl";
        try (FileOutputStream fos = new FileOutputStream(crlPath)) {
            fos.write(crl.getEncoded());
        }

        System.out.println("✅ CRL generated and saved at: " + crlPath);
        return crl;
    }
}
