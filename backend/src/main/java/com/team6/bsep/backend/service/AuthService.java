package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.*;
import com.team6.bsep.backend.model.*;
import com.team6.bsep.backend.repository.CertificateAuthorityRepository;
import com.team6.bsep.backend.repository.PasswordResetTokenRepository;
import com.team6.bsep.backend.repository.UserRepository;
import com.team6.bsep.backend.repository.VerificationTokenRepository;
import com.team6.bsep.backend.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordResetTokenRepository passwordResetTokens;
    private final CertificateAuthorityRepository caRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    // koliko traje aktivacioni link (u satima)
    @Value("${app.activation.expiry-hours:24}")
    private long expiryHours;

    // za logovanje kompletnog linka u dev-u
    @Value("${app.backend-base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.reset.expiry-minutes:60}")
    private long resetExpiryMinutes;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl; //posto link iz mejla mora da odvede na Angular formu

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TokenUtils tokenUtils;

    public Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();
    private Map<String, String> jtiToJwtMap = new ConcurrentHashMap<>();

    @Autowired
    private CaptchaService captchaService;


    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordEncoder encoder,
                       EmailService emailService,
                       PasswordResetTokenRepository passwordResetTokens,
                       CertificateAuthorityRepository caRepo) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.emailService = emailService;
        this.passwordResetTokens = passwordResetTokens;
        this.caRepo = caRepo;
    }


    @Transactional
    public void register(RegisterRequest req) {
        // normalizuj email da izbegneš duplikate zbog velikih/malih slova
        var email = req.getEmail().trim().toLowerCase();

        // 1) duplikat email-a (brza provera)
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 2) heš lozinke i kreiranje korisnika (status = PENDING po default-u iz modela)
        var user = User.builder()
                .email(email)
                .passwordHash(encoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .organization(req.getOrganization())
                .build();

        try {
            users.save(user); // INSERT users
        } catch (DataIntegrityViolationException e) {
            // fallback ako se desi "trka" i udari unique constraint
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 3) generiši aktivacioni token (jednokratan, vremenski ograničen)
        var tokenValue = UUID.randomUUID().toString();
        var token = VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(expiryHours)))
                .build();

        tokens.save(token); // INSERT verification_tokens

        // 4) za razvoj: ispiši kompletan link u log
        var activationLink = backendBaseUrl + "/api/auth/verify?token=" + tokenValue;
        try {
            emailService.sendActivation(email, activationLink);
        } catch (org.springframework.mail.MailException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Sending activation link failed. Please, try again."
            );
        }

    }

    @Transactional
    public void verify(String tokenValue) {
        var token = tokens.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (token.getUsedAt() != null)
            throw new ResponseStatusException(HttpStatus.GONE, "Token already used");

        if (Instant.now().isAfter(token.getExpiresAt()))
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");

        var user = token.getUser();
        user.setStatus(com.team6.bsep.backend.model.UserStatus.ACTIVE);
        user.setActivatedAt(Instant.now());
        token.setUsedAt(Instant.now());
    }

    public ResponseEntity<?> login(String email, String password, String recaptchaToken) {
        try {
            boolean captchaOk = captchaService.verifyCaptcha(recaptchaToken);
            if (!captchaOk) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("CAPTCHA validation failed");
            }

            Authentication authentication = authenticateUser(email, password);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jti = UUID.randomUUID().toString();
            var user = users.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String jwt = tokenUtils.generateToken(user);
            int expiresIn = tokenUtils.getExpiredIn();

            String role = user.getRole().name();

            registerActiveToken(jti, jwt);

            log.info("Login successful for email: {}, IP: {}, User-Agent: {}", email,
                    request.getRemoteAddr(), request.getHeader("User-Agent"));

            return ResponseEntity.ok(new JwtResponse(jwt, expiresIn, jti, role, user.isMustChangePassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong email or password");
        }
    }


    private Authentication authenticateUser(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }

    private void registerActiveToken(String jti, String jwt) {
        TokenInfo tokenInfo = new TokenInfo(
                jti,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                LocalDateTime.now()
        );
        activeTokens.put(jti, tokenInfo);
        jtiToJwtMap.put(jti, jwt);
    }

    @Transactional
    public void initiatePasswordReset(String rawEmail) {
        var email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
        Optional<User> maybeUser = users.findByEmail(email);

        if (maybeUser.isEmpty()) {
            log.info("Password reset requested for non-existing email: {}", email);
            return;
        }
        var user = maybeUser.get();
        if (user.getStatus() != com.team6.bsep.backend.model.UserStatus.ACTIVE) {
            log.info("Password reset requested for non-active user: {}", email);
            return;
        }

        passwordResetTokens.deleteByUserAndUsedAtIsNull(user);

        var tokenValue = UUID.randomUUID().toString();
        var token = PasswordResetToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(resetExpiryMinutes)))
                .build();

        passwordResetTokens.save(token);

        var link = frontendBaseUrl + "/reset-password?token=" + tokenValue;

        try {
            emailService.sendPasswordReset(email, link, resetExpiryMinutes);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Failed sending reset email to {}", email, ex);
            // Ne otkrivamo ništa klijentu; kontroler i dalje vraća 200
        }

        log.info("Password reset initiated for email: {}", email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        var token = passwordResetTokens.findByToken(req.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (token.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token already used");
        }

        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token expired");
        }

        var user = token.getUser();
        user.setPasswordHash(encoder.encode(req.getNewPassword()));

        token.setUsedAt(Instant.now());

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional
    public void createCaUser(CreateCaUserRequest req) {
        String normalized = req.email().trim().toLowerCase();

        if (users.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 1. generiši random lozinku
        String rawPassword = UUID.randomUUID().toString().substring(0, 12);

        // 2. kreiraj user-a sa CA rolom
        var user = User.builder()
                .email(normalized)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .organization(req.organization())
                .passwordHash(encoder.encode(rawPassword))
                .role(com.team6.bsep.backend.model.UserRole.CA)
                .status(com.team6.bsep.backend.model.UserStatus.ACTIVE)
                .mustChangePassword(true)
                .status(UserStatus.ACTIVE)
                .activatedAt(Instant.now())
                .build();

        var caEntity = CertificateAuthority.builder()
                .subjectDn("CN=" + req.organization() + " CA, O=" + req.organization() + ", C=RS")
                .root(false)
                .notBefore(Instant.now())
                .notAfter(Instant.now().plus(365, ChronoUnit.DAYS))
                .build();

        caRepo.save(caEntity);

        // 3) poveži usera i CA
        user.setCertificateAuthority(caEntity);
        users.save(user);

        // 3. pošalji mejl
        emailService.sendCaUserCreated(normalized, rawPassword, req.firstName());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        var user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!encoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        log.info("Password changed for user {}", user.getEmail());
    }
}

