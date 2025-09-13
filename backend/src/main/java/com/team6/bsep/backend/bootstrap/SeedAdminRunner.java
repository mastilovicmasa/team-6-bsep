package com.team6.bsep.backend.bootstrap;

import com.team6.bsep.backend.model.User;
import com.team6.bsep.backend.model.UserRole;
import com.team6.bsep.backend.model.UserStatus;
import com.team6.bsep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SeedAdminRunner implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.seed.admin.email}")            String adminEmail;
    @Value("${app.seed.admin.password}")         String adminPassword;
    @Value("${app.seed.admin.first-name:System}")   String adminFirstName;
    @Value("${app.seed.admin.last-name:Administrator}") String adminLastName;
    @Value("${app.seed.admin.organization:BSEP}")     String adminOrg;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("APP_SEED_ADMIN_PASSWORD not set.");
        }

        users.findByEmail(adminEmail).ifPresentOrElse(
                u -> System.out.println("[seed] Admin already exists: " + adminEmail),
                () -> {
                    User admin = new User();
                    admin.setEmail(adminEmail.trim().toLowerCase());
                    admin.setPasswordHash(encoder.encode(adminPassword));

                    admin.setFirstName(adminFirstName);
                    admin.setLastName(adminLastName);
                    admin.setOrganization(adminOrg);


                    admin.setRole(UserRole.ADMIN);
                    try {
                        admin.setStatus(UserStatus.ACTIVE);
                    } catch (IllegalArgumentException | NoSuchFieldError ignored) {
                    }

                    admin.setActivatedAt(Instant.now());

                    users.save(admin);
                    System.out.println("=== INITIAL ADMIN CREATED === " + adminEmail);
                }
        );
    }
}
