package com.safetrip.backend.infrastructure.integration.notification.resend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Email {
    private String to;
    private String subject;
    private String body;
    private List<String> cc;
    private List<String> bcc;
    private boolean isHtml;
    private List<EmailAttachment> attachments; // <-- NUEVO

    public Email() {
        this.cc = new ArrayList<>();
        this.bcc = new ArrayList<>();
        this.attachments = new ArrayList<>();
        this.isHtml = false;
    }


    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(EmailAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("El attachment no puede ser null");
        }
        if (attachment.getFilename() == null || attachment.getFilename().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }
        if (attachment.getContent() == null || attachment.getContent().length == 0) {
            throw new IllegalArgumentException("El contenido del archivo no puede estar vacío");
        }
        this.attachments.add(attachment);
    }

    public void validate() {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'to' es requerido y no puede estar vacío");
        }
        if (!to.contains("@")) {
            throw new IllegalArgumentException("El email 'to' tiene un formato inválido");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'subject' es requerido y no puede estar vacío");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'body' es requerido y no puede estar vacío");
        }
        if (cc != null) {
            for (String email : cc) {
                if (email == null || !email.contains("@")) {
                    throw new IllegalArgumentException("El email en 'cc' tiene un formato inválido: " + email);
                }
            }
        }
        if (bcc != null) {
            for (String email : bcc) {
                if (email == null || !email.contains("@")) {
                    throw new IllegalArgumentException("El email en 'bcc' tiene un formato inválido: " + email);
                }
            }
        }
    }

    public static class Builder {
        private String to;
        private String subject;
        private String body;
        private List<String> cc = new ArrayList<>();
        private List<String> bcc = new ArrayList<>();
        private boolean isHtml = false;
        private List<EmailAttachment> attachments = new ArrayList<>();

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder cc(List<String> cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(List<String> bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder isHtml(boolean isHtml) {
            this.isHtml = isHtml;
            return this;
        }

        public Builder attachments(List<EmailAttachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public Builder addAttachment(EmailAttachment attachment) {
            if (attachment == null) {
                throw new IllegalArgumentException("El attachment no puede ser null");
            }
            if (attachment.getFilename() == null || attachment.getFilename().isEmpty()) {
                throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
            }
            if (attachment.getContent() == null || attachment.getContent().length == 0) {
                throw new IllegalArgumentException("El contenido del archivo no puede estar vacío");
            }
            this.attachments.add(attachment);
            return this;
        }

        public Email build() {
            Email email = new Email();
            email.to = this.to;
            email.subject = this.subject;
            email.body = this.body;
            email.cc = this.cc;
            email.bcc = this.bcc;
            email.isHtml = this.isHtml;
            email.attachments = this.attachments;
            return email;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
