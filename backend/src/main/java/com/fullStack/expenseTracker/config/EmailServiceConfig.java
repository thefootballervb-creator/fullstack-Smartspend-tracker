package com.fullStack.expenseTracker.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailServiceConfig {

    /**
     * This configuration allows switching between email providers.
     * 
     * To use SendGrid:
     * - Set email.provider=sendgrid in application.properties
     * - Configure sendgrid.api.key and sendgrid.from.email
     * 
     * To use SMTP (default):
     * - Set email.provider=smtp (or omit it)
     * - Configure spring.mail.* properties
     */
}

