package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.*;
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
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201
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
    public ResponseEntity<?> createCaUser(@Valid @RequestBody CreateCaUserRequest req) {
        auth.createCaUser(req);
        return ResponseEntity.ok("CA user created and password sent to email");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest req) {
        auth.changePassword(req);
        return ResponseEntity.ok().build();
    }

}
