package com.safetrip.backend.infrastructure.integration.notification;

import com.safetrip.backend.application.dto.NotificationDTO;
import com.safetrip.backend.domain.service.NotificationService;
import com.safetrip.backend.domain.service.TemplateProvider;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailtrapNotificationServiceImpl implements NotificationService {

    private final MailtrapClient client;
    private final TemplateProvider templateProvider;

    public MailtrapNotificationServiceImpl(
            @Value("${mailtrap.token}") String token,
            @Value("${mailtrap.inbox}") Long inboxId,
            TemplateProvider templateProvider
    ) {
        MailtrapConfig config = new MailtrapConfig.Builder()
                .sandbox(true)
                .inboxId(inboxId)
                .token(token)
                .build();

        this.client = MailtrapClientFactory.createMailtrapClient(config);
        this.templateProvider = templateProvider;
    }

    @Override
    public void send(NotificationDTO notification) {
        String htmlContent = templateProvider.getOtpTemplate(notification.getMessage());

        MailtrapMail mail = MailtrapMail.builder()
                .from(new Address("hello@example.com", "Safetrip"))
                .to(List.of(new Address(notification.getRecipient())))
                .subject(notification.getSubject())
                .html(htmlContent) // <<<<<< HTML en vez de text
                .category("Safetrip Notification")
                .build();

        try {
            client.send(mail);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo con Mailtrap", e);
        }
    }
}