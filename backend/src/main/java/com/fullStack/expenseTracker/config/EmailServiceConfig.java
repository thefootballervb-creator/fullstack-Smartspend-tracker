package com.fullStack.expenseTracker.config;

import java.util.Arrays;
import java.util.Objects;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;

/**
 * Central configuration for the application's pluggable email providers.
 *
 * By default, outbound email is disabled (see {@code NoopNotificationService}).
 * When {@code email.provider=smtp}, this configuration guarantees that a
 * {@link JavaMailSender} bean exists so that the application can start even if
 * SMTP credentials are missing. In that case, a no-op mail sender is registered
 * which simply logs the attempts instead of sending emails.
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(MailProperties.class)
public class EmailServiceConfig {

    @Bean
    @ConditionalOnMissingBean(MailProperties.class)
    @ConfigurationProperties(prefix = "spring.mail")
    public MailProperties mailProperties() {
        return new MailProperties();
    }

    @Bean
    @ConditionalOnProperty(name = "email.provider", havingValue = "smtp")
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        if (!StringUtils.hasText(mailProperties.getHost())) {
            log.warn("""
                    email.provider is set to 'smtp' but no SMTP host has been configured. \
                    Falling back to a no-op JavaMailSender. \
                    Set spring.mail.host (or SPRING_MAIL_HOST) and related credentials to deliver real emails.
                    """);
            return new NoopJavaMailSender(mailProperties);
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        applyMailProperties(mailProperties, sender);

        log.info("Configured JavaMailSender with host '{}' and port {}", mailProperties.getHost(), mailProperties.getPort());
        return sender;
    }

    private void applyMailProperties(MailProperties source, JavaMailSenderImpl target) {
        target.setHost(source.getHost());
        if (source.getPort() != null) {
            target.setPort(source.getPort());
        }
        if (StringUtils.hasText(source.getProtocol())) {
            target.setProtocol(source.getProtocol());
        }
        if (StringUtils.hasText(source.getUsername())) {
            target.setUsername(source.getUsername());
        }
        if (StringUtils.hasText(source.getPassword())) {
            target.setPassword(source.getPassword());
        }
        if (source.getDefaultEncoding() != null) {
            target.setDefaultEncoding(source.getDefaultEncoding().name());
        }
        if (!source.getProperties().isEmpty()) {
            target.getJavaMailProperties().putAll(source.getProperties());
        }
    }

    /**
     * {@link JavaMailSender} implementation that gracefully drops outbound email
     * attempts while still allowing the application context to start.
     */
    static final class NoopJavaMailSender extends JavaMailSenderImpl {

        NoopJavaMailSender(MailProperties mailProperties) {
            if (mailProperties.getDefaultEncoding() != null) {
                setDefaultEncoding(mailProperties.getDefaultEncoding().name());
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            logDroppedMimeMessages("single message", mimeMessage);
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            logDroppedMimeMessages("multiple messages", (Object[]) mimeMessages);
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            MimeMessage mimeMessage = createMimeMessage();
            try {
                mimeMessagePreparator.prepare(mimeMessage);
                logDroppedMimeMessages("prepared message", mimeMessage);
            } catch (Exception ex) {
                throw new MailPreparationException("Failed to prepare MimeMessage for noop mail sender", ex);
            }
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            Arrays.stream(mimeMessagePreparators).forEach(this::send);
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            logDroppedSimpleMessages("single message", simpleMessage);
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            logDroppedSimpleMessages("multiple messages", (Object[]) simpleMessages);
        }

        private void logDroppedMimeMessages(String description, Object... messages) {
            log.warn("Dropping {} because SMTP is not configured (email.provider=smtp)", description);
            Arrays.stream(messages)
                    .filter(Objects::nonNull)
                    .forEach(message -> log.debug("Dropped message metadata: {}", extractMimeSummary(message)));
        }

        private void logDroppedSimpleMessages(String description, Object... messages) {
            log.warn("Dropping {} because SMTP is not configured (email.provider=smtp)", description);
            Arrays.stream(messages)
                    .filter(Objects::nonNull)
                    .forEach(message -> log.debug("Dropped message metadata: {}", message));
        }

        private String extractMimeSummary(Object mimeMessage) {
            if (mimeMessage instanceof MimeMessage message) {
                try {
                    return "subject='%s' to=%s".formatted(
                            message.getSubject(),
                            Arrays.toString(message.getRecipients(MimeMessage.RecipientType.TO))
                    );
                } catch (MessagingException ex) {
                    return "unavailable (failed to read subject/recipients: " + ex.getMessage() + ")";
                }
            }
            return String.valueOf(mimeMessage);
        }
    }
}

