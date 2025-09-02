package com.team6.bsep.backend.service;

import com.team6.bsep.backend.dto.RegisterRequest;
import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.VerificationToken;
import com.team6.bsep.backend.repository.UserRepository;
import com.team6.bsep.backend.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordEncoder encoder;

    // koliko traje aktivacioni link (u satima)
    @Value("${app.activation.expiry-hours:24}")
    private long expiryHours;

    // za logovanje kompletnog linka u dev-u
    @Value("${app.backend-base-url:http://localhost:8080}")
    private String backendBaseUrl;

    public AuthService(UserRepository users,
                       VerificationTokenRepository tokens,
                       PasswordEncoder encoder) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
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
        log.info("Activation link for {}: {}", email, activationLink);
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
}

