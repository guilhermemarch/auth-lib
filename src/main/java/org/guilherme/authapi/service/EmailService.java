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
        String subject = "Verificação de Email";
        String verificationUrl = appConfig.getVerification().getVerificationUrl(token);

        String content = """
<html>
  <body style=\"margin: 0; padding: 0; background-color: #f2f4f6; font-family: Arial, sans-serif;\">
    <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f2f4f6; padding: 40px 0;\">
      <tr>
        <td align=\"center\">
          <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; padding: 30px; border-radius: 6px;\">
            <tr>
              <td align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #2f2f2f; padding-bottom: 16px;\">
                Verifique seu endereço de email
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 16px; color: #4a4a4a; padding-bottom: 12px;\">
                Obrigado por se cadastrar!
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 16px; color: #4a4a4a; padding-bottom: 24px;\">
                Clique no botão abaixo para verificar seu email e ativar sua conta:
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"padding-bottom: 24px;\">
                <a href=\"%s\" style=\"background-color: #007bff; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; display: inline-block; font-weight: bold;\">
                  Verificar Email
                </a>
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 14px; color: #4a4a4a; padding-bottom: 12px;\">
                Se o botão não funcionar, copie e cole este link no seu navegador:
              </td>
            </tr>
            <tr>
              <td align=\"center\">
                <a href=\"%s\" style=\"color: #007bff; text-decoration: none;\">%s</a>
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 12px; color: #999999; padding-top: 30px;\">
                Este link irá expirar em %d hora%s.<br />
                Se você não solicitou isso, pode ignorar este email.
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
        String subject = "Solicitação de Redefinição de Senha";
        String resetUrl = appConfig.getVerification().getResetUrl(token);

        String content = """
<html>
  <body style=\"margin: 0; padding: 0; background-color: #f2f4f6; font-family: Arial, sans-serif;\">
    <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f2f4f6; padding: 40px 0;\">
      <tr>
        <td align=\"center\">
          <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; padding: 30px; border-radius: 6px;\">
            <tr>
              <td align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #2f2f2f; padding-bottom: 16px;\">
                Redefina sua senha
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 16px; color: #4a4a4a; padding-bottom: 12px;\">
                Recebemos uma solicitação para redefinir sua senha.
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 16px; color: #4a4a4a; padding-bottom: 24px;\">
                Clique no botão abaixo para criar uma nova senha:
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"padding-bottom: 24px;\">
                <a href=\"%s\" style=\"background-color: #007bff; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; display: inline-block; font-weight: bold;\">
                  Redefinir Senha
                </a>
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 14px; color: #4a4a4a; padding-bottom: 12px;\">
                Se o botão não funcionar, copie e cole este link no seu navegador:
              </td>
            </tr>
            <tr>
              <td align=\"center\">
                <a href=\"%s\" style=\"color: #007bff; text-decoration: none;\">%s</a>
              </td>
            </tr>
            <tr>
              <td align=\"center\" style=\"font-size: 12px; color: #999999; padding-top: 30px;\">
                Este link irá expirar em %d hora%s.<br />
                Se você não solicitou a redefinição de senha, pode ignorar este email.
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