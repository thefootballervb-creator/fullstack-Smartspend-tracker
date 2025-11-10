package com.fullStack.expenseTracker.services.impls;

import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.services.NotificationService;
import jakarta.mail.MessagingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * Fallback implementation that disables outbound email when {@code email.provider}
 * is set to {@code disabled} (default). It allows the application to start
 * without provisioning an SMTP/SendGrid client in non-email deployments.
 */
@Component
@ConditionalOnProperty(
        name = "email.provider",
        havingValue = "disabled",
        matchIfMissing = true
)
public class NoopNotificationService implements NotificationService {

    @Override
    public void sendUserRegistrationVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        // Intentionally no-op
    }

    @Override
    public void sendForgotPasswordVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        // Intentionally no-op
    }
}

