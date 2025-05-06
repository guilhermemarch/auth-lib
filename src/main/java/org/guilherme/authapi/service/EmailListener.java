package org.guilherme.authapi.service;

import jakarta.mail.MessagingException;
import org.guilherme.authapi.dto.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailListener {

    private static final Logger logger = LoggerFactory.getLogger(EmailListener.class);

    private final EmailService emailService;

    @Value("${app.rabbitmq.queue.email}")
    private String emailQueue;

    @Autowired
    public EmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.email}")
    public void receiveEmailMessage(EmailMessage emailMessage) {
        logger.info("Received email message for: {}", emailMessage.getTo());
        try {
            emailService.processEmailMessage(emailMessage);
            logger.info("Email sent successfully to: {}", emailMessage.getTo());
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email to: {}", emailMessage.getTo(), e);
            // precisa implementar retry aqui
        }
    }
} 