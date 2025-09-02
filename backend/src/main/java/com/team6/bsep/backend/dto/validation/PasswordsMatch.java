package com.team6.bsep.backend.dto.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Imena polja u DTO-u (možeš promeniti ako koristiš drugačija)
    String passwordField() default "password";
    String confirmField() default "confirmPassword";
}
