package com.team6.bsep.backend.service;

import com.team6.bsep.backend.model.RevokedCertificate;
import com.team6.bsep.backend.repository.RevokedCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevocationService {

    private final RevokedCertificateRepository repository;

    public void revokeCertificate(String serialNumber, String reason) {
        if (repository.existsBySerialNumber(serialNumber)) {
            throw new IllegalStateException("Certificate already revoked");
        }
        repository.save(new RevokedCertificate(serialNumber, reason));
    }

    public boolean isRevoked(String serialNumber) {
        return repository.existsBySerialNumber(serialNumber);
    }

    public X509CRL generateCRL(X509Certificate issuerCert, PrivateKey issuerKey) throws Exception {
        X509v2CRLBuilder builder = new X509v2CRLBuilder(
                new org.bouncycastle.asn1.x500.X500Name(issuerCert.getSubjectX500Principal().getName()),
                new Date()
        );

        List<RevokedCertificate> revokedList = repository.findAll();
        for (RevokedCertificate rc : revokedList) {
            builder.addCRLEntry(
                    new BigInteger(rc.getSerialNumber()),
                    Date.from(rc.getRevokedAt().atZone(ZoneId.systemDefault()).toInstant()),
                    CRLReason.lookup(0)
            );
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey);
        return new JcaX509CRLConverter().getCRL(builder.build(signer));
    }
}
