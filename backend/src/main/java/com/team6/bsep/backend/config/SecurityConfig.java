package com.team6.bsep.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        int saltLength = 16;     // bytes
        int hashLength = 32;     // bytes
        int parallelism = 1;     // threads
        int memory = 4096;       // KB (4 MB)
        int iterations = 3;      // passes

        return new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())               // stateless API
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",          // register, verify (otključano)
                                "/error",                // da ne blokira default error handler
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" // ako koristiš Swagger
                        ).permitAll()
                        .anyRequest().authenticated() // sve ostalo zaključano (priprema za JWT)
                );
        return http.build();
    }
}
