package com.safetrip.backend.infrastructure.integration.notification.resend.client;

import com.safetrip.backend.domain.exception.EmailException;
import com.safetrip.backend.domain.model.Payment;
import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.Policy;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.model.enums.DocumentType;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.Email;
import com.safetrip.backend.infrastructure.integration.notification.resend.dto.EmailAttachment;
import com.safetrip.backend.infrastructure.integration.notification.resend.service.EmailService;
import com.safetrip.backend.infrastructure.integration.notification.resend.service.EmailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cliente interno para el envío de emails
 * Proporciona métodos de alto nivel para enviar diferentes tipos de emails
 */
@Component
public class EmailClient {

    private static final Logger logger = LoggerFactory.getLogger(EmailClient.class);

    @Value("${mail.to.policy}")
    private String mailToPolicy;

    private final EmailService emailService;
    private final EmailTemplateService templateService;

    public EmailClient(EmailService emailService,
                       EmailTemplateService templateService) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    /**
     * Envía un email simple de texto
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .isHtml(false)
                    .build();

            email.validate();
            emailService.sendEmail(email);
            logger.info("Email simple enviado a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email simple a: {}", to, e);
            throw new EmailException("No se pudo enviar el email simple", e);
        }
    }

    public void sendPolicyPdfToCustomer(Policy policy, Payment payment, byte[] pdfBytes) {
        try {
            Person person = policy.getCreatedByUser().getPerson();
            User user = policy.getCreatedByUser();

            if (person == null || user == null) {
                logger.warn("No se pudo obtener información del usuario para póliza: {}", policy.getPolicyId());
                return;
            }

            String customerEmail = user.getEmail();
            String customerName = person.getFullName() != null ? person.getFullName() : "Cliente";
            String transactionId = payment != null && payment.getTransactionId() != null ?
                    payment.getTransactionId() : "N/A";

            // Construir el body del email para el cliente
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(String.format("Estimado/a %s,\n\n", customerName));
            bodyBuilder.append("¡Tu póliza ha sido confirmada exitosamente!\n\n");
            bodyBuilder.append("DETALLES DE TU PÓLIZA:\n");
            bodyBuilder.append("========================\n\n");
            bodyBuilder.append(String.format(
                    "Número de Póliza: %s\n",
                    policy.getPolicyNumber() != null ? policy.getPolicyNumber() : policy.getPolicyId()
            ));
            bodyBuilder.append(String.format("Monto Pagado: $%s\n\n", payment.getAmount()));
            bodyBuilder.append("Adjunto encontrarás tu póliza en formato PDF.\n\n");
            bodyBuilder.append("Gracias por confiar en SafeTrip.\n\n");
            bodyBuilder.append("Saludos cordiales,\n");
            bodyBuilder.append("Equipo SafeTrip");

            Email email = Email.builder()
                    .to(customerEmail)
                    .subject(String.format("✅ Tu Póliza SafeTrip - Confirmación de Pago #%s", transactionId))
                    .body(bodyBuilder.toString())
                    .isHtml(false)
                    .build();

            // Agregar el PDF como adjunto
            if (pdfBytes != null && pdfBytes.length > 0) {
                EmailAttachment pdfAttachment = new EmailAttachment(
                        String.format("poliza-%s.pdf", policy.getPolicyId()),
                        pdfBytes,
                        "application/pdf"
                );
                email.addAttachment(pdfAttachment);
                logger.info("PDF adjunto agregado al correo del cliente: {} bytes", pdfBytes.length);
            }

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("✅ Email con PDF de póliza enviado exitosamente al cliente: {} para póliza: {}",
                    customerEmail, policy.getPolicyId());

        } catch (Exception e) {
            logger.error("❌ Error al enviar email con PDF al cliente para póliza: {}",
                    policy.getPolicyId(), e);
            throw new EmailException("No se pudo enviar el email con PDF al cliente", e);
        }
    }

    /**
     * Envía un email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            email.validate();
            emailService.sendEmail(email);
            logger.info("Email HTML enviado a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email HTML a: {}", to, e);
            throw new EmailException("No se pudo enviar el email HTML", e);
        }
    }

    /**
     * Envía un email de forma asíncrona
     */
    public void sendEmailAsync(String to, String subject, String body, boolean isHtml) {
        try {
            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .isHtml(isHtml)
                    .build();

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email asíncrono programado para: {}", to);
        } catch (Exception e) {
            logger.error("Error al programar email asíncrono para: {}", to, e);
            throw new EmailException("No se pudo programar el envío del email", e);
        }
    }

    /**
     * Envía un email de bienvenida usando plantilla
     */
    public void sendWelcomeEmail(String to, String name) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("name", name);

            String htmlBody = templateService.processTemplate(
                    templateService.getWelcomeTemplate(),
                    variables
            );

            Email email = Email.builder()
                    .to(to)
                    .subject("¡Bienvenido a nuestra plataforma!")
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email de bienvenida enviado a: {} ({})", to, name);
        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida a: {}", to, e);
            throw new EmailException("No se pudo enviar el email de bienvenida", e);
        }
    }

    /**
     * Envía un email de restablecimiento de contraseña
     */
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("resetLink", resetLink);

            String htmlBody = templateService.processTemplate(
                    templateService.getPasswordResetTemplate(),
                    variables
            );

            Email email = Email.builder()
                    .to(to)
                    .subject("Restablece tu contraseña")
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email de restablecimiento de contraseña enviado a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email de restablecimiento a: {}", to, e);
            throw new EmailException("No se pudo enviar el email de restablecimiento", e);
        }
    }

    /**
     * Envía un email con código OTP
     */
    public void sendOtpEmail(String to, String userName, String otpCode, int expiryMinutes) {
        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("otpCode", otpCode);
            variables.put("expiryMinutes", String.valueOf(expiryMinutes));

            String htmlBody = templateService.processTemplate(
                    templateService.getOtpTemplate(),
                    variables
            );

            Email email = Email.builder()
                    .to(to)
                    .subject("Tu código de verificación - Safetrip")
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email OTP enviado a: {} con código: {}", to, otpCode);
        } catch (Exception e) {
            logger.error("Error al enviar email OTP a: {}", to, e);
            throw new EmailException("No se pudo enviar el email OTP", e);
        }
    }

    /**
     * Envía un email con plantilla personalizada
     */
    public void sendTemplatedEmail(String to, String subject, String template, Map<String, String> variables) {
        try {
            String htmlBody = templateService.processTemplate(template, variables);

            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .body(htmlBody)
                    .isHtml(true)
                    .build();

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email con plantilla personalizada enviado a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email con plantilla a: {}", to, e);
            throw new EmailException("No se pudo enviar el email con plantilla", e);
        }
    }

    /**
     * Envía un email con copia (CC) y copia oculta (BCC)
     */
    public void sendEmailWithCopies(String to, String subject, String body,
                                    java.util.List<String> cc, java.util.List<String> bcc,
                                    boolean isHtml) {
        try {
            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .cc(cc)
                    .bcc(bcc)
                    .isHtml(isHtml)
                    .build();

            email.validate();
            emailService.sendEmail(email);
            logger.info("Email con copias enviado a: {}", to);
        } catch (Exception e) {
            logger.error("Error al enviar email con copias a: {}", to, e);
            throw new EmailException("No se pudo enviar el email con copias", e);
        }
    }

    /**
     * Envía un email de póliza creada con información del usuario, voucher y archivos adjuntos
     * Nuevo método para enviar email de póliza con archivos adjuntos
     */
    public void sendPolicyEmail(Policy policy, Payment payment, List<EmailAttachment> attachments) {
        try {
            Person person = policy.getCreatedByUser().getPerson();
            if (person == null) {
                logger.warn("No se pudo obtener información de la persona para póliza: {}", policy.getPolicyId());
                return;
            }

            DocumentType documentType = person.getDocumentType() != null ? person.getDocumentType() : DocumentType.CC;
            String documentNumber = person.getDocumentNumber() != null ? person.getDocumentNumber() : "N/A";
            String fullName = person.getFullName() != null ? person.getFullName() : "N/A";
            String transactionId = payment != null && payment.getTransactionId() != null ?
                    payment.getTransactionId() : "N/A";

            // Construir el body del email sin template
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("INFORMACIÓN DE PÓLIZA\n");
            bodyBuilder.append("====================\n\n");
            bodyBuilder.append(String.format("Tipo Documento: %s\n", documentType));
            bodyBuilder.append(String.format("Número Documento: %s\n", documentNumber));
            bodyBuilder.append(String.format("Nombre Completo: %s\n", fullName));
            bodyBuilder.append(String.format("VOUCHER: %s\n", transactionId));

            Email email = Email.builder()
                    .to(mailToPolicy)
                    .subject(String.format("Póliza Creada - %s - %s", documentNumber, fullName))
                    .body(bodyBuilder.toString())
                    .isHtml(false)
                    .build();

            // Agregar archivos adjuntos validados
            if (attachments != null && !attachments.isEmpty()) {
                for (EmailAttachment attachment : attachments) {
                    try {
                        email.addAttachment(attachment);
                        logger.info("Archivo adjunto agregado: {}", attachment.getFilename());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Archivo rechazado para póliza {}: {}", policy.getPolicyId(), e.getMessage());
                    }
                }
            }

            email.validate();
            emailService.sendEmailAsync(email);
            logger.info("Email de póliza enviado exitosamente para póliza: {} a: {}", policy.getPolicyId(), "alejandrogallego030@gmail.com");

        } catch (Exception e) {
            logger.error("Error al enviar email de póliza para ID: {}", policy.getPolicyId(), e);
            throw new EmailException("No se pudo enviar el email de póliza", e);
        }
    }
}