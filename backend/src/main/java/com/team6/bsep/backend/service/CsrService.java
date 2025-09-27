package com.team6.bsep.backend.service;
import com.team6.bsep.backend.dto.CaInfo;
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
        var caInfo = findCaInfo(request.getCaName());
        validateDuration(request.getDurationInDays(), caInfo.getNotAfter());

        String email = resolveCurrentUserEmail();
        var userEntity = findUserByEmail(email);

        ParsedCsr parsed = parseAndLog(request.getCsrFile());
        var caEntity = findCaById(caInfo.getId());

        CertificateSigningRequest entity = buildCsrEntity(request, parsed, caEntity, userEntity);
        requestRepo.save(entity);

        log.info("CSR saved in DB with ID={}", entity.getId());
    }

    private CaInfo findCaInfo(String caName) {
        return caRepo.findProjectedBySubjectDn(caName)
                .orElseThrow(() -> new IllegalArgumentException("CA not found: " + caName));
    }

    private void validateDuration(int durationDays, Instant caNotAfter) {
        Instant requestedEnd = Instant.now().plus(Duration.ofDays(durationDays));
        if (requestedEnd.isAfter(caNotAfter)) {
            throw new IllegalArgumentException("Certificate duration exceeds CA validity (" + caNotAfter + ")");
        }
    }

    private String resolveCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    private com.team6.bsep.backend.model.User findUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    private CertificateAuthority findCaById(Long caId) {
        return caRepo.findById(caId)
                .orElseThrow(() -> new IllegalArgumentException("CA not found by ID: " + caId));
    }

    private CertificateSigningRequest buildCsrEntity(
            CsrRequest request,
            ParsedCsr parsed,
            CertificateAuthority caEntity,
            com.team6.bsep.backend.model.User userEntity
    ) throws Exception {
        String pemText = new String(request.getCsrFile().getBytes(), StandardCharsets.UTF_8);

        return CertificateSigningRequest.builder()
                .csrPem(pemText)
                .durationInDays(request.getDurationInDays())
                .status(RequestStatus.PENDING)
                .ca(caEntity)
                .user(userEntity)
                .subjectCn(parsed.cn())
                .subjectO(parsed.o())
                .subjectC(parsed.c())
                .createdAt(Instant.now())
                .build();
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
            PKCS10CertificationRequest csr = parseCsrFromPem(csrEntity.getCsrPem());

            var caEntity = loadRootCa();
            String rootCaPassword = crypto.decrypt(caEntity.getKeystorePasswordEnc());

            KeyStore keyStore = loadKeyStore(caEntity.getKeystorePath(), rootCaPassword);
            PrivateKey caPrivateKey = getPrivateKey(keyStore, caEntity.getKeystoreAlias(), rootCaPassword);
            X509Certificate caCert = getCertificate(keyStore, caEntity.getKeystoreAlias());

            X509Certificate eeCert = issueCertificate(csr, caCert, caPrivateKey, csrEntity.getDurationInDays());
            String certPem = convertToPem(eeCert);

            saveEndEntityCertificate(caEntity, csrEntity, eeCert, certPem);
            markCsrAsIssued(csrEntity);

            log.info("Issued certificate:\n{}", certPem);

        } catch (Exception e) {
            log.error("Failed to issue certificate", e);
            throw new RuntimeException("Failed to issue certificate: " + e.getMessage(), e);
        }
    }


    private PKCS10CertificationRequest parseCsrFromPem(String pem) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pem))) {
            return (PKCS10CertificationRequest) pemParser.readObject();
        }
    }

    private CertificateAuthority loadRootCa() {
        return caRepo.findByRootTrue()
                .orElseThrow(() -> new RuntimeException("Root CA not found"));
    }

    private KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }
        return keyStore;
    }

    private PrivateKey getPrivateKey(KeyStore keyStore, String alias, String password) throws Exception {
        return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    }

    private X509Certificate getCertificate(KeyStore keyStore, String alias) throws Exception {
        return (X509Certificate) keyStore.getCertificate(alias);
    }

    private X509Certificate issueCertificate(PKCS10CertificationRequest csr, X509Certificate caCert,
                                             PrivateKey caPrivateKey, int durationDays) throws Exception {
        X500Name issuer = new X500Name(caCert.getSubjectX500Principal().getName());
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = Date.from(Instant.now().plus(durationDays, ChronoUnit.DAYS));

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, csr.getSubject(), csr.getSubjectPublicKeyInfo());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(caPrivateKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certHolder);
    }

    private String convertToPem(X509Certificate cert) throws Exception {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            pemWriter.writeObject(cert);
        }
        return sw.toString();
    }

    private void saveEndEntityCertificate(CertificateAuthority caEntity,
                                          CertificateSigningRequest csrEntity,
                                          X509Certificate eeCert, String pem) {
        var eeCertEntity = EndEntityCertificate.builder()
                .serialHex(eeCert.getSerialNumber().toString(16))
                .pem(pem)
                .notBefore(eeCert.getNotBefore().toInstant())
                .notAfter(eeCert.getNotAfter().toInstant())
                .issuer(caEntity)
                .csr(csrEntity)
                .build();

        endEntityCertificateRepository.save(eeCertEntity);
    }

    private void markCsrAsIssued(CertificateSigningRequest csrEntity) {
        csrEntity.setStatus(RequestStatus.ISSUED);
        requestRepo.save(csrEntity);
    }

    public EndEntityCertificate getCertificateForCsr(Long csrId) {
        return endEntityCertificateRepository.findByCsrId(csrId)
                .orElseThrow(() -> new RuntimeException("Certificate not found for CSR " + csrId));
    }

    @Transactional(readOnly = true)
    public byte[] getCertificatePem(Long csrId) {
        var cert = getCertificateForCsr(csrId);
        return cert.getPem().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public List<MyCsr> findRequestsForCaUser(String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        var ca = user.getCertificateAuthority();
        if (ca == null) {
            throw new RuntimeException("User is not linked to any Certificate Authority");
        }

        return requestRepo.findByCaId(ca.getId());
    }

    @Transactional
    public void approveRequestAsCa(Long csrId, String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        var ca = user.getCertificateAuthority();
        if (ca == null) {
            throw new RuntimeException("User is not linked to any Certificate Authority");
        }

        // sad koristiš ca entitet i njegov keystore da izdaš sertifikat
        CertificateSigningRequest csrEntity = requestRepo.findById(csrId)
                .orElseThrow(() -> new RuntimeException("CSR not found"));

        if (csrEntity.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("CSR already processed");
        }

        try {
            // isto kao approveRequest, ali sa CA korisnikom
            PKCS10CertificationRequest csr = parseCsrFromPem(csrEntity.getCsrPem());

            String caPassword = crypto.decrypt(ca.getKeystorePasswordEnc());
            KeyStore keyStore = loadKeyStore(ca.getKeystorePath(), caPassword);
            PrivateKey caPrivateKey = getPrivateKey(keyStore, ca.getKeystoreAlias(), caPassword);
            X509Certificate caCert = getCertificate(keyStore, ca.getKeystoreAlias());

            X509Certificate eeCert = issueCertificate(csr, caCert, caPrivateKey, csrEntity.getDurationInDays());
            String certPem = convertToPem(eeCert);

            saveEndEntityCertificate(ca, csrEntity, eeCert, certPem);
            markCsrAsIssued(csrEntity);

        } catch (Exception e) {
            throw new RuntimeException("Failed to issue certificate by CA: " + e.getMessage(), e);
        }
    }


}
