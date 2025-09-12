package com.team6.bsep.backend.controller;

import com.team6.bsep.backend.dto.LoginRequest;
import com.team6.bsep.backend.dto.RegisterRequest;
import com.team6.bsep.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
