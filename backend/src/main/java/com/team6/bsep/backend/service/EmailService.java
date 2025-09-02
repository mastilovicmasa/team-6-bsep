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
}
