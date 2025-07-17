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
  <body style="margin: 0; padding: 0; background-color: #f2f4f6; font-family: Arial, sans-serif;">
    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f2f4f6; padding: 40px 0;">
      <tr>
        <td align="center">
          <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; padding: 30px; border-radius: 6px;">
            <tr>
              <td align="center" style="font-size: 24px; font-weight: bold; color: #2f2f2f; padding-bottom: 16px;">
                Verify Your Email Address
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 16px; color: #4a4a4a; padding-bottom: 12px;">
                Thanks for signing up!
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 16px; color: #4a4a4a; padding-bottom: 24px;">
                Click the button below to verify your email and activate your account:
              </td>
            </tr>
            <tr>
              <td align="center" style="padding-bottom: 24px;">
                <a href="%s" style="background-color: #007bff; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; display: inline-block; font-weight: bold;">
                  Verify Email
                </a>
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 14px; color: #4a4a4a; padding-bottom: 12px;">
                If the button doesn't work, copy and paste this link into your browser:
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 14px; word-break: break-all; color: #007bff;">
                <a href="%s" style="color: #007bff; text-decoration: none;">%s</a>
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 12px; color: #999999; padding-top: 30px;">
                This link will expire in %d hour%s.<br />
                If you didn't request this, you can ignore this email.
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
""".formatted(
                verificationUrl,
                verificationUrl,
                verificationUrl,
                appConfig.getVerification().getTokenExpirationHours(),
                appConfig.getVerification().getTokenExpirationHours() == 1 ? "" : "s"
        );

        EmailMessage emailMessage = new EmailMessage(to, subject, content, token, "VERIFICATION");
        
        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, emailMessage);
    }

    public void sendChangePasswordEmail(String to, String token) throws MessagingException, UnsupportedEncodingException {
        String subject = "Password Reset Request";
        String resetUrl = appConfig.getVerification().getResetUrl(token);

        String content = """
<html>
  <body style="margin: 0; padding: 0; background-color: #f2f4f6; font-family: Arial, sans-serif;">
    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f2f4f6; padding: 40px 0;">
      <tr>
        <td align="center">
          <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; padding: 30px; border-radius: 6px;">
            <tr>
              <td align="center" style="font-size: 24px; font-weight: bold; color: #2f2f2f; padding-bottom: 16px;">
                Reset Your Password
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 16px; color: #4a4a4a; padding-bottom: 12px;">
                We received a request to reset your password.
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 16px; color: #4a4a4a; padding-bottom: 24px;">
                Click the button below to set a new password:
              </td>
            </tr>
            <tr>
              <td align="center" style="padding-bottom: 24px;">
                <a href="%s" style="background-color: #007bff; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; display: inline-block; font-weight: bold;">
                  Reset Password
                </a>
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 14px; color: #4a4a4a; padding-bottom: 12px;">
                If the button doesn't work, copy and paste this link into your browser:
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 14px; word-break: break-all; color: #007bff;">
                <a href="%s" style="color: #007bff; text-decoration: none;">%s</a>
              </td>
            </tr>
            <tr>
              <td align="center" style="font-size: 12px; color: #999999; padding-top: 30px;">
                This link will expire in %d hour%s.<br />
                If you did not request a password reset, you can safely ignore this email.
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
""".formatted(
                resetUrl,
                resetUrl,
                resetUrl,
                appConfig.getVerification().getTokenExpirationHours(),
                appConfig.getVerification().getTokenExpirationHours() == 1 ? "" : "s"
        );

        EmailMessage emailMessage = new EmailMessage(to, subject, content, token, "RESET_PASSWORD");

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