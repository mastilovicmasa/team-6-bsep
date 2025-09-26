package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.ForgotPasswordRequest;
import com.team6.bsep.backend.dto.LoginRequest;
import com.team6.bsep.backend.dto.RegisterRequest;
import com.team6.bsep.backend.dto.ResetPasswordRequest;
import com.team6.bsep.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req);                // baca 400/409 po potrebi
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 bez tela
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        auth.verify(token);
        return ResponseEntity.ok("Account activated");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return auth.login(
                request.getEmail(),
                request.getPassword(),
                request.getRecaptchaToken()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        auth.initiatePasswordReset(req.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/create-ca-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCaUser(@RequestParam String email) {
        auth.createCaUser(email);
        return ResponseEntity.ok("CA user created and password sent to email");
    }

}
