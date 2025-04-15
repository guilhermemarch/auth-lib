package org.guilherme.authapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    private final AppConfig appConfig;

    @Autowired
    public MailConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(appConfig.getMail().getHost());
        mailSender.setPort(appConfig.getMail().getPort());
        mailSender.setUsername(appConfig.getMail().getUsername());
        mailSender.setPassword(appConfig.getMail().getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", appConfig.getMail().isSmtpAuth());
        props.put("mail.smtp.starttls.enable", appConfig.getMail().isStarttlsEnable());
        props.put("mail.smtp.timeout", 5000);
        props.put("mail.smtp.connectiontimeout", 5000);
        props.put("mail.smtp.writetimeout", 5000);
        props.put("mail.debug", "false");

        return mailSender;
    }
} 