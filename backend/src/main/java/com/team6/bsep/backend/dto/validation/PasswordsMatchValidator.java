package com.team6.bsep.backend.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, Object> {
    private String passwordField;
    private String confirmField;

    @Override
    public void initialize(PasswordsMatch ann) {
        this.passwordField = ann.passwordField();
        this.confirmField  = ann.confirmField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) return true; // @Valid DTO obično nije null; stvarnu prazninu hvata @NotBlank na poljima

        var wrapper = new BeanWrapperImpl(value);
        Object pwObj = wrapper.getPropertyValue(passwordField);
        Object cfObj = wrapper.getPropertyValue(confirmField);

        String pw = pwObj == null ? null : pwObj.toString();
        String cf = cfObj == null ? null : cfObj.toString();

        boolean matches = pw != null && pw.equals(cf);

        if (!matches) {
            // usmeri poruku na konkretno polje confirmPassword (lepše za klijenta)
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(confirmField)
                    .addConstraintViolation();
        }
        return matches;
    }
}
