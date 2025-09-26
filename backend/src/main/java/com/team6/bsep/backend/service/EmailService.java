package com.team6.bsep.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@team6.local}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendActivation(String to, String activationLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Activate your account");
        msg.setText("""
                Hello,
                
                Thank you for registering. To activate your account, please click on link bellow:
                %s

                Link lasts 24 hours and can be used only once.
                """.formatted(activationLink));
        mailSender.send(msg);
    }

    public void sendPasswordReset(String to, String resetLink, long expiryMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Reset your password");
        msg.setText("""
            Hello,

            A password reset was requested for your account.
            To reset your password, please click the link below:

            %s

            The link is valid for %d minutes and can be used only once.
            If you did not request a password reset, you can safely ignore this message.
            """.formatted(resetLink, expiryMinutes));
        mailSender.send(msg);
    }

    public void sendCaUserPassword(String to, String rawPassword) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Your CA user account credentials");
        msg.setText("""
        Hello,

        A new CA user account has been created for you.
        You can log in with the following temporary password:

        %s

        For security reasons, you will be required to change this password
        immediately upon your first login.

        Best regards,
        PKI System
        """.formatted(rawPassword));
        mailSender.send(msg);
    }

}
