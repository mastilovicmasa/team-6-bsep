package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.CsrRequest;
import com.team6.bsep.backend.dto.MyCsr;
import com.team6.bsep.backend.dto.ParsedCsr;
import com.team6.bsep.backend.model.CertificateAuthority;
import com.team6.bsep.backend.model.CertificateSigningRequest;
import com.team6.bsep.backend.model.EndEntityCertificate;
import com.team6.bsep.backend.model.RequestStatus;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.CertificateSigningRequestRepository;
import com.team6.bsep.backend.repository.EndEntityCertificateRepository;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsrService {
    private static final Logger log = LoggerFactory.getLogger(CsrService.class);

    private final CertificateAuthorityRepository caRepo;
    private final CertificateSigningRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final EndEntityCertificateRepository endEntityCertificateRepository;
    private final CryptoService crypto;

    static {
        Security.addProvider(new BouncyCastleProvider());
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

    @Transactional
    public void approveRequest(Long id) {
        CertificateSigningRequest csrEntity = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("CSR not found"));

        if (csrEntity.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("CSR already processed");
        }

        try {
            // 1. Parse CSR iz PEM stringa
            String csrPem = csrEntity.getCsrPem();
            PEMParser pemParser = new PEMParser(new StringReader(csrPem));
            PKCS10CertificationRequest csr = (PKCS10CertificationRequest) pemParser.readObject();
            pemParser.close();

            // 2. Učitaj Root CA entitet i dešifruj lozinku
            var caEntity = caRepo.findByRootTrue()
                    .orElseThrow(() -> new RuntimeException("Root CA not found"));
            String rootCaPassword = crypto.decrypt(caEntity.getKeystorePasswordEnc());

            // 3. Učitaj Root CA keystore sa dešifrovanom lozinkom
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(caEntity.getKeystorePath())) {
                keyStore.load(fis, rootCaPassword.toCharArray());
            }

            PrivateKey caPrivateKey = (PrivateKey) keyStore.getKey(
                    caEntity.getKeystoreAlias(),
                    rootCaPassword.toCharArray()
            );
            X509Certificate caCert = (X509Certificate) keyStore.getCertificate(caEntity.getKeystoreAlias());

            // 4. Napravi generator sertifikata
            X500Name issuer = new X500Name(caCert.getSubjectX500Principal().getName());
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date notBefore = new Date();
            Date notAfter = Date.from(Instant.now().plus(csrEntity.getDurationInDays(), ChronoUnit.DAYS));

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                    issuer,
                    serial,
                    notBefore,
                    notAfter,
                    csr.getSubject(),
                    csr.getSubjectPublicKeyInfo()
            );

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .build(caPrivateKey);

            X509CertificateHolder certHolder = certBuilder.build(signer);
            X509Certificate eeCert = new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider())
                    .getCertificate(certHolder);

            // 5. Konvertuj u PEM string
            StringWriter sw = new StringWriter();
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
                pemWriter.writeObject(eeCert);
            }
            String certPem = sw.toString();

            // 6. Snimi EE sertifikat u bazu
            var eeCertEntity = EndEntityCertificate.builder()
                    .serialHex(serial.toString(16))
                    .pem(certPem)
                    .notBefore(notBefore.toInstant())
                    .notAfter(notAfter.toInstant())
                    .issuer(caEntity)
                    .csr(csrEntity)
                    .build();

            endEntityCertificateRepository.save(eeCertEntity);

            // 7. Ažuriraj CSR
            csrEntity.setStatus(RequestStatus.ISSUED);
            requestRepo.save(csrEntity);

            log.info("Issued certificate:\n{}", certPem);

        } catch (Exception e) {
            log.error("Failed to issue certificate", e);
            throw new RuntimeException("Failed to issue certificate: " + e.getMessage(), e);
        }
    }

}
