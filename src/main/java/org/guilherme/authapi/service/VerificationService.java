package org.guilherme.authapi.service;

import jakarta.mail.MessagingException;
import org.guilherme.authapi.config.AppConfig;
import org.guilherme.authapi.entity.User;
import org.guilherme.authapi.entity.VerificationToken;
import org.guilherme.authapi.exception.TokenExpiredException;
import org.guilherme.authapi.exception.TokenNotFoundException;
import org.guilherme.authapi.repository.UserRepository;
import org.guilherme.authapi.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Service
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppConfig appConfig;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public VerificationService(
            VerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailService emailService,
            AppConfig appConfig, PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.appConfig = appConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createVerificationToken(User user) {
        VerificationToken token = new VerificationToken(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(appConfig.getVerification().getTokenExpirationHours()));
        tokenRepository.save(token);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), token.getToken());
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Transactional
   public void createPasswordResetToken(User user) {
        VerificationToken token = new VerificationToken(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(appConfig.getVerification().getTokenExpirationHours()));
        tokenRepository.save(token);

        try {
            emailService.sendChangePasswordEmail(user.getEmail(), token.getToken());
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }


    @Transactional
    public User verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Verification token not found"));
        
        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Verification token has expired");
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);
        
        verificationToken.setVerified(true);
        tokenRepository.save(verificationToken);
        
        return user;
    }
    
    @Transactional
    public void resendVerificationToken(User user) {
        tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .forEach(tokenRepository::delete);
        
        createVerificationToken(user);
    }


    public User resetPassword(String token, String newPassword) {

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password must not be empty");
        }

        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Password reset token not found"));

        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Password reset token has expired");
        }


        User user = verificationToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

       // user.setPassword(newPassword);

        userRepository.save(user);

        verificationToken.setVerified(true);
        tokenRepository.save(verificationToken);
        return user;
    }

} 