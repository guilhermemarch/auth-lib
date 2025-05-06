package org.guilherme.authapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.guilherme.authapi.config.AppConfig;
import org.guilherme.authapi.dto.EmailMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppConfig appConfig;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.email}")
    private String emailExchange;

    @Value("${app.rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Autowired
    public EmailService(JavaMailSender mailSender, AppConfig appConfig, RabbitTemplate rabbitTemplate) {
        this.mailSender = mailSender;
        this.appConfig = appConfig;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendVerificationEmail(String to, String token) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";
        String verificationUrl = appConfig.getVerification().getVerificationUrl(token);

        String content = """
    <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f9f9f9;
                    padding: 20px;
                    color: #333333;
                }
                .container {
                    background-color: #ffffff;
                    padding: 20px;
                    border-radius: 8px;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    max-width: 600px;
                    margin: auto;
                }
                .button {
                    display: inline-block;
                    margin-top: 20px;
                    padding: 10px 20px;
                    font-size: 16px;
                    color: #ffffff;
                    background-color: #007BFF;
                    text-decoration: none;
                    border-radius: 5px;
                }
                p {
                    line-height: 1.6;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>Verify Your Email Address</h2>
                <p>Thank you for registering! Please verify your email address by clicking the button below:</p>
                <a href="%s" class="button">Verify Email</a>
                <p>If the button above doesn't work, you can also copy and paste the following link into your browser:</p>
                <p><a href="%s">%s</a></p>
                <p>This link will expire in %d hours.</p>
            </div>
        </body>
    </html>
""".formatted(verificationUrl, verificationUrl, verificationUrl, appConfig.getVerification().getTokenExpirationHours());


        EmailMessage emailMessage = new EmailMessage(to, subject, content, token, "VERIFICATION");
        
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, emailMessage);
    }
    
    public void processEmailMessage(EmailMessage emailMessage) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        
        helper.setText(emailMessage.getContent(), true);
        helper.setTo(emailMessage.getTo());
        helper.setSubject(emailMessage.getSubject());
        
        if (appConfig.getMail().getFromAddress() != null) {
            helper.setFrom(appConfig.getMail().getFromAddress(), appConfig.getMail().getFromName());
        }
        
        mailSender.send(mimeMessage);
    }
} 