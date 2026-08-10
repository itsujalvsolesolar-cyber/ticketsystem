package com.sujal.itsm.core.email.service;

import com.sujal.itsm.core.admin.service.SystemConfigurationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SystemConfigurationService configService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from:noreply@ticketsystem.com}")
    private String defaultFrom;

    /**
     * Send HTML email asynchronously (non-blocking)
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Object templateData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            if (templateData != null) {
                context.setVariable("data", templateData);
            }

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email sent to {}: {}", to, subject);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send simple text email (fallback)
     */
    @Async
    public void sendTextEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
            log.info("✅ Text email sent to {}: {}", to, subject);

        } catch (MessagingException e) {
            log.error("❌ Failed to send text email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a test email using dynamic SMTP settings from the database.
     */
    public void sendTestEmail(String recipientEmail) throws Exception { // ✅ Changed to throws Exception
        log.info("📧 Sending dynamic test email to {}", recipientEmail);

        // 1. Fetch settings from the cached database configuration
        String host = configService.getSetting("EMAIL", "smtp_host", "smtp.gmail.com");
        String port = configService.getSetting("EMAIL", "smtp_port", "587");
        String username = configService.getSetting("EMAIL", "smtp_username", "");
        String password = configService.getSetting("EMAIL", "smtp_password", "");
        String tls = configService.getSetting("EMAIL", "smtp_tls_enabled", "true");
        String senderName = configService.getSetting("EMAIL", "sender_name", "ITSM System");
        String senderEmail = configService.getSetting("EMAIL", "sender_email", username);

        if (username.isEmpty() || password.isEmpty()) {
            throw new RuntimeException("SMTP Username and Password must be configured in Email Settings first.");
        }

        // 2. Create a temporary, dynamic JavaMailSender
        JavaMailSenderImpl dynamicMailSender = new JavaMailSenderImpl();
        dynamicMailSender.setHost(host);
        dynamicMailSender.setPort(Integer.parseInt(port));
        dynamicMailSender.setUsername(username);
        dynamicMailSender.setPassword(password);

        Properties props = dynamicMailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", tls);
        props.put("mail.smtp.ssl.enable", !Boolean.parseBoolean(tls));

        // 3. Build and send the message
        MimeMessage message = dynamicMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(senderEmail, senderName);
        helper.setTo(recipientEmail);
        helper.setSubject("✅ Test Email: ITSM Admin Console");
        helper.setText("<h3 style='color: green;'>Success!</h3>" +
                "<p>This is a test email sent from your System Administration Console.</p>" +
                "<p>If you received this, your dynamic SMTP configuration is working perfectly.</p>" +
                "<hr><small>Server: " + host + ":" + port + "</small>", true);

        dynamicMailSender.send(message);
        log.info("✅ Dynamic test email sent successfully.");
    }
}