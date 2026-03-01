package com.safetrip.backend.infrastructure.integration.notification.resend.service;

import com.safetrip.backend.domain.exception.EmailException;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.Email;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de email usando Spring Mail y SMTP (Resend)
 * Soporta envío síncrono y asincrónico con archivos adjuntos
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:onboarding@resend.dev}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Envía un email de forma síncrona
     */
    public void sendEmail(Email email) {
        try {
            email.validate();
            MimeMessage message = createMimeMessage(email);
            mailSender.send(message);
            logger.info("Email enviado exitosamente a: {} con {} adjuntos",
                    email.getTo(),
                    email.getAttachments() != null ? email.getAttachments().size() : 0);
        } catch (MessagingException e) {
            logger.error("Error al enviar email a: {}", email.getTo(), e);
            throw new EmailException("No se pudo enviar el email", e);
        }
    }

    /**
     * Envía un email de forma asíncrona
     */
    @Async
    public void sendEmailAsync(Email email) {
        try {
            sendEmail(email);
        } catch (Exception e) {
            logger.error("Error al enviar email asíncrono a: {}", email.getTo(), e);
        }
    }

    /**
     * Crear MimeMessage con soporte completo para attachments desde MinIO
     */
    private MimeMessage createMimeMessage(Email email) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(email.getTo());
        helper.setSubject(email.getSubject());
        helper.setText(email.getBody(), email.isHtml());

        if (email.getCc() != null && !email.getCc().isEmpty()) {
            helper.setCc(email.getCc().toArray(new String[0]));
        }

        if (email.getBcc() != null && !email.getBcc().isEmpty()) {
            helper.setBcc(email.getBcc().toArray(new String[0]));
        }

        if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
            for (EmailAttachment attachment : email.getAttachments()) {
                try {
                    helper.addAttachment(attachment.getFilename(),
                            new ByteArrayResource(attachment.getContent()),
                            attachment.getContentType());
                    logger.debug("Archivo adjunto agregado: {}", attachment.getFilename());
                } catch (Exception e) {
                    logger.warn("Error al adjuntar archivo {}: {}", attachment.getFilename(), e.getMessage());
                    throw new MessagingException("Error al procesar archivo adjunto: " + attachment.getFilename(), e);
                }
            }
        }

        return message;
    }
}
