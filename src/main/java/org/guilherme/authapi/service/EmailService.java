package org.guilherme.authapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.guilherme.authapi.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppConfig appConfig;

    @Autowired
    public EmailService(JavaMailSender mailSender, AppConfig appConfig) {
        this.mailSender = mailSender;
        this.appConfig = appConfig;
    }

    public void sendVerificationEmail(String to, String token) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        
        String subject = "Email Verification";
        String verificationUrl = appConfig.getVerification().getVerificationUrl(token);
        
        String content = """
            <html>
                <body>
                    <h2>Verify your email address</h2>
                    <p>Thank you for registering. Please click on the link below to verify your email address:</p>
                    <a href="%s">Verify Email</a>
                    <p>This link will expire in %d hours.</p>
                </body>
            </html>
        """.formatted(verificationUrl, appConfig.getVerification().getTokenExpirationHours());
        
        helper.setText(content, true);
        helper.setTo(to);
        helper.setSubject(subject);
        
        if (appConfig.getMail().getFromAddress() != null) {
            helper.setFrom(appConfig.getMail().getFromAddress(), appConfig.getMail().getFromName());
        }
        
        mailSender.send(mimeMessage);
    }
} 