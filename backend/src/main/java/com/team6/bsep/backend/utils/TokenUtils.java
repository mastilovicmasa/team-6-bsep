package com.team6.bsep.backend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtils {

    @Value("${spring.application.name}")
    private String APP_NAME;

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.expires-in:1800000}")
    private int EXPIRES_IN;

    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String generateToken(String email) {
        return Jwts.builder()
                .setIssuer(APP_NAME)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRES_IN))
                .signWith(SIGNATURE_ALGORITHM, SECRET)
                .compact();
    }

    public int getExpiredIn() {
        return EXPIRES_IN;
    }
}
