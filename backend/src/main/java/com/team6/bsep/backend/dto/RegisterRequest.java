package com.team6.bsep.backend.dto;

import com.team6.bsep.backend.dto.validation.PasswordsMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Email
    @Size(max = 254)              // realan max za email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String organization;

    @NotBlank
    @Size(min = 12, max = 128)    // OWASP stil: dužina, ne regex-kompleksnost
    private String password;

    @NotBlank
    private String confirmPassword;

    // obavezno prazan ctor za Jackson
    public RegisterRequest() {}

    // (opciono) sve-args ctor, ako želiš
    public RegisterRequest(String email, String firstName, String lastName,
                           String organization, String password, String confirmPassword) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organization = organization;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // getteri/setteri
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
