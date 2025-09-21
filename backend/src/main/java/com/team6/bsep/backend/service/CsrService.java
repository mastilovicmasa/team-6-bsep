package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.security.PublicKey;
import java.security.Security;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CsrService {

    private final CertificateAuthorityRepository caRepo;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public void parseAndLog(MultipartFile csrFile) throws Exception {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(csrFile.getInputStream()))) {
            Object parsedObj = pemParser.readObject();
            if (!(parsedObj instanceof PKCS10CertificationRequest csr)) {
                throw new IllegalArgumentException("Invalid CSR format");
            }

            // Subject (X500Name)
            X500Name subject = csr.getSubject();
            String cn = getRdnValue(subject, BCStyle.CN);
            String o = getRdnValue(subject, BCStyle.O);
            String c = getRdnValue(subject, BCStyle.C);

            System.out.println("✅ CSR parsed successfully!");
            System.out.println("CN = " + cn);
            System.out.println("O  = " + o);
            System.out.println("C  = " + c);

            PublicKey publicKey =
                    java.security.KeyFactory.getInstance("RSA")
                            .generatePublic(new java.security.spec.X509EncodedKeySpec(
                                    csr.getSubjectPublicKeyInfo().getEncoded()));

            System.out.println("Public Key Algorithm = " + publicKey.getAlgorithm());
            System.out.println("Public Key Format    = " + publicKey.getFormat());
        }
    }

    private String getRdnValue(X500Name x500Name, org.bouncycastle.asn1.ASN1ObjectIdentifier field) {
        RDN[] rdns = x500Name.getRDNs(field);
        if (rdns != null && rdns.length > 0) {
            return IETFUtils.valueToString(rdns[0].getFirst().getValue());
        }
        return "";
    }

    public void processCsr(CsrRequest request) throws Exception {
        String caName = request.getCaName();
        int duration = request.getDurationInDays();

        var ca = caRepo.findBySubjectDn(caName)
                .orElseThrow(() -> new IllegalArgumentException("CA not found: " + caName));

        Instant now = Instant.now();
        Instant requestedEnd = now.plus(Duration.ofDays(duration));
        if (requestedEnd.isAfter(ca.getNotAfter())) {
            throw new IllegalArgumentException("❌ Certificate duration exceeds CA validity (" + ca.getNotAfter() + ")");
        }

        parseAndLog(request.getCsrFile());

        // ovde bi u sledećem koraku išlo i potpisivanje CSR-a
    }
}
