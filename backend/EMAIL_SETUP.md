# Email Provider Configuration

This project supports multiple outbound email providers via the `email.provider`
property. The default is `disabled`, which switches the backend into a safe
no-email mode (`NoopNotificationService`). Use one of the following options to
enable real email delivery.

---

## Option 1 · SMTP Relay (e.g. MailerSend, Mailtrap, Gmail)

1. **Set the provider**

   ```
   email.provider=smtp
   ```

2. **Provide SMTP credentials** using Spring Boot's `spring.mail.*` properties.
   You can define them in `backend/src/main/resources/application.properties`
   (local development only) or with environment variables in production.

   | Property                    | Environment variable      | Description                                      |
   |-----------------------------|---------------------------|--------------------------------------------------|
   | `spring.mail.host`          | `SPRING_MAIL_HOST`        | SMTP hostname (e.g. `smtp.mailersend.net`)       |
   | `spring.mail.port`          | `SPRING_MAIL_PORT`        | SMTP port (MailerSend uses `587`)                |
   | `spring.mail.username`      | `SPRING_MAIL_USERNAME`    | SMTP username                                    |
   | `spring.mail.password`      | `SPRING_MAIL_PASSWORD`    | SMTP password / API token                        |
   | `spring.mail.protocol`      | `SPRING_MAIL_PROTOCOL`    | Usually `smtp`                                   |
   | `spring.mail.properties.mail.smtp.auth` | `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | Set to `true` when authentication is required |
   | `spring.mail.properties.mail.smtp.starttls.enable` | `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | Set to `true` for TLS |
   | `spring.mail.from.email`    | `SPRING_MAIL_FROM_EMAIL`  | From address shown to recipients                 |
   | `spring.mail.from.name`     | `SPRING_MAIL_FROM_NAME`   | Friendly sender name                             |

   > Tip (MailerSend): Dashboard → **Senders & domains → SMTP users** to create
   > a username/password pair. Copy host, port, username, and password into the
   > corresponding environment variables above.

3. **Restart the backend** so that Spring picks up the new configuration.

4. **Verify delivery** by running the signup or password reset flow. The logs
   (`backend/backend.log`) will show the SMTP handshake details. If no SMTP host
   is configured the backend will fall back to a no-op `JavaMailSender` and log
   a warning, but the application will still start.

---

## Option 2 · SendGrid API

1. **Set the provider**

   ```
   email.provider=sendgrid
   ```

2. **Configure SendGrid environment variables**

   | Environment variable  | Description                                                  |
   |-----------------------|--------------------------------------------------------------|
   | `SENDGRID_API_KEY`    | SendGrid API key with Mail Send permissions                  |
   | `SENDGRID_FROM_EMAIL` | Verified sender address                                      |
   | `SENDGRID_FROM_NAME`  | Friendly sender name (optional, defaults to `Company`)       |

3. **Export variables before starting the backend**

   ```powershell
   setx EMAIL_PROVIDER sendgrid
   setx SENDGRID_API_KEY "<your-api-key>"
   setx SENDGRID_FROM_EMAIL "no-reply@example.com"
   setx SENDGRID_FROM_NAME "SmartSpend"
   ```

4. **Restart the backend**. The `SendGridEmailService` will now handle all mail
   via the SendGrid REST API.

---

## Local Development Defaults

- Database and other secrets should stay in environment variables. The values in
  `application.properties` are only fallbacks for local testing.
- When no email provider is configured, the backend uses
  `NoopNotificationService`, so signup/reset flows will succeed without sending
  real emails.

---

## Troubleshooting

- **Warning: Dropping messages because SMTP is not configured…**
  The backend created a fallback `JavaMailSender`. Double-check your `spring.mail.*`
  variables.

- **`jakarta.mail.AuthenticationFailedException`**
  Username or password is incorrect, or your provider requires TLS/SSL—set the
  appropriate `spring.mail.properties.mail.smtp.*` flags.

- **SendGrid 401 errors**
  Confirm the API key has Mail Send permissions and was exported in the
  environment where the backend runs.

