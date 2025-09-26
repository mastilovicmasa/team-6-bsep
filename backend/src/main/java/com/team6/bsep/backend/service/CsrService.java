package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.dto.MyCsr;
import com.team6.bsep.backend.dto.ParsedCsr;
import com.team6.bsep.backend.model.CertificateSigningRequest;
import com.team6.bsep.backend.model.RequestStatus;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.CertificateSigningRequestRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsrService {

    private final CertificateAuthorityRepository caRepo;
    private final CertificateSigningRequestRepository requestRepo;
    private final UserRepository userRepo;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public ParsedCsr parseAndLog(MultipartFile csrFile) throws Exception {
        try (PEMParser pemParser = new PEMParser(new InputStreamReader(csrFile.getInputStream()))) {
            Object parsedObj = pemParser.readObject();
            if (!(parsedObj instanceof PKCS10CertificationRequest csr)) {
                throw new IllegalArgumentException("Invalid CSR format");
            }

            X500Name subject = csr.getSubject();
            String cn = getRdnValue(subject, BCStyle.CN);
            String o = getRdnValue(subject, BCStyle.O);
            String c = getRdnValue(subject, BCStyle.C);

            System.out.println("CSR parsed successfully!");
            System.out.println("CN = " + cn);
            System.out.println("O  = " + o);
            System.out.println("C  = " + c);

            PublicKey publicKey =
                    java.security.KeyFactory.getInstance("RSA")
                            .generatePublic(new java.security.spec.X509EncodedKeySpec(
                                    csr.getSubjectPublicKeyInfo().getEncoded()));

            int keySize = ((java.security.interfaces.RSAPublicKey) publicKey).getModulus().bitLength();

            System.out.println("Public Key Algorithm = " + publicKey.getAlgorithm());
            System.out.println("Public Key Format    = " + publicKey.getFormat());
            System.out.println("Public Key Size      = " + keySize + " bits");

            var attrs = csr.getAttributes();
            boolean hasExtensions = false;

            for (var attr : attrs) {
                if (attr.getAttrType().equals(org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                    hasExtensions = true;
                    var extSeq = (org.bouncycastle.asn1.x509.Extensions) attr.getAttrValues().getObjectAt(0);

                    var oids = extSeq.oids();
                    while (oids.hasMoreElements()) {
                        var oid = (org.bouncycastle.asn1.ASN1ObjectIdentifier) oids.nextElement();
                        var ext = extSeq.getExtension(oid);
                        System.out.println("Extension: " + oid.getId() + " critical=" + ext.isCritical());
                    }
                }
            }

            if (!hasExtensions) {
                System.out.println("No extensions found in CSR.");
            }

            return new ParsedCsr(cn, o, c);
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

        var caInfo = caRepo.findProjectedBySubjectDn(caName)
                .orElseThrow(() -> new IllegalArgumentException("CA not found: " + caName));

        Instant now = Instant.now();
        Instant requestedEnd = now.plus(Duration.ofDays(duration));
        if (requestedEnd.isAfter(caInfo.getNotAfter())) {
            throw new IllegalArgumentException(
                    "Certificate duration exceeds CA validity (" + caInfo.getNotAfter() + ")"
            );
        }

        // ➡️ 1. Uzimamo ulogovanog korisnika
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        var userEntity = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        ParsedCsr parsed = parseAndLog(request.getCsrFile());

        var caEntity = caRepo.findById(caInfo.getId())
                .orElseThrow(() -> new IllegalArgumentException("CA not found by ID: " + caInfo.getId()));


        String pemText = new String(request.getCsrFile().getBytes(), StandardCharsets.UTF_8);

        CertificateSigningRequest entity = CertificateSigningRequest.builder()
                .csrPem(pemText)
                .durationInDays(duration)
                .status(RequestStatus.PENDING)
                .ca(caEntity)
                .user(userEntity)
                .subjectCn(parsed.cn())
                .subjectO(parsed.o())
                .subjectC(parsed.c())
                .createdAt(now)
                .build();

        requestRepo.save(entity);
        System.out.println("CSR saved in DB with ID=" + entity.getId());
    }

    public List<MyCsr> getRequestsForUser(String email) {
        return requestRepo.findMyRequestsByUserEmail(email);
    }

    public List<MyCsr> getAllRequests() {
        return requestRepo.findAllRequests();
    }
}
