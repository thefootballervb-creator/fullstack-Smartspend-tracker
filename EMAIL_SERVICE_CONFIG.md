# Email Service Configuration Guide

## Current Setup
The application is currently using **Mailtrap** (sandbox/testing environment).

## Available Options

### Option 1: SMTP (Generic SMTP Server)
Use any SMTP server (Gmail, Outlook, custom SMTP, etc.)

**Configuration in `application.properties`:**
```properties
# Gmail SMTP Example
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Outlook SMTP Example
# spring.mail.host=smtp-mail.outlook.com
# spring.mail.port=587
# spring.mail.username=your-email@outlook.com
# spring.mail.password=your-password
# spring.mail.properties.mail.smtp.auth=true
# spring.mail.properties.mail.smtp.starttls.enable=true
```

**Note:** For Gmail, you need to use an "App Password" instead of your regular password.

---

### Option 2: SendGrid (Recommended)
Professional email service with API-based sending using SendGrid Java SDK.

**Step 1:** Sign up at https://sendgrid.com and get your API key
   - Go to https://app.sendgrid.com/settings/api_keys
   - Create a new API key with "Full Access" or "Mail Send" permissions
   - Copy the API key (you won't be able to see it again!)

**Step 2:** Verify a sender email address
   - Go to https://app.sendgrid.com/settings/sender_auth
   - Verify a single sender or set up domain authentication

**Step 3:** Update `application.properties`:**
```properties
# Switch to SendGrid
email.provider=sendgrid

# SendGrid Configuration
sendgrid.api.key=YOUR_SENDGRID_API_KEY_HERE
sendgrid.from.email=noreply@yourdomain.com
sendgrid.from.name=Company
```

**Step 4:** Replace `YOUR_SENDGRID_API_KEY_HERE` with your actual SendGrid API key

**Benefits:**
- High deliverability and reliability
- Free tier: 100 emails/day forever
- Detailed analytics and tracking
- Native API integration (not SMTP)
- Better error handling and response codes
- Easy to scale as your needs grow

---

### Option 3: Mailgun
Professional email service with excellent deliverability.

**Step 1:** Sign up at https://www.mailgun.com and get your credentials

**Step 2:** Update `application.properties`:**
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=YOUR_MAILGUN_SMTP_USERNAME
spring.mail.password=YOUR_MAILGUN_SMTP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Step 3:** Get credentials from Mailgun Dashboard → Sending → Domain Settings → SMTP credentials

**Benefits:**
- Free tier: 5,000 emails/month (first 3 months)
- Excellent deliverability
- API and SMTP support
- Detailed analytics

---

## Current Configuration (Mailtrap - Testing)
```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=747072e194cece
spring.mail.password=9b0c38304e088c
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Note:** Mailtrap is for testing only - emails don't actually get sent. Use one of the above options for production.

---

## How to Switch

### Switching to SendGrid:
1. Sign up at https://sendgrid.com and get your API key
2. Verify a sender email address in SendGrid dashboard
3. Update `application.properties`:
   - Set `email.provider=sendgrid`
   - Add `sendgrid.api.key=YOUR_API_KEY`
   - Add `sendgrid.from.email=your-verified-email@domain.com`
4. Restart the backend server
5. Test by registering a new user or resetting a password

### Switching to SMTP:
1. Set `email.provider=smtp` (or remove the property - it's the default)
2. Configure `spring.mail.*` properties
3. Restart the backend server
4. Test by registering a new user or resetting a password

---

## Security Best Practices

1. **Never commit credentials to version control**
   - Use environment variables or a `.env` file
   - Add `application.properties` to `.gitignore` if it contains sensitive data

2. **Use App Passwords for Gmail**
   - Enable 2FA on your Google account
   - Generate an app-specific password

3. **Rotate API keys regularly**
   - Especially for SendGrid and Mailgun

4. **Use environment variables (Recommended)**
   
   For SMTP:
   ```properties
   spring.mail.host=${MAIL_HOST}
   spring.mail.port=${MAIL_PORT}
   spring.mail.username=${MAIL_USERNAME}
   spring.mail.password=${MAIL_PASSWORD}
   ```
   
   For SendGrid:
   ```properties
   email.provider=${EMAIL_PROVIDER}
   sendgrid.api.key=${SENDGRID_API_KEY}
   sendgrid.from.email=${SENDGRID_FROM_EMAIL}
   sendgrid.from.name=${SENDGRID_FROM_NAME}
   ```

---

## Testing

After configuring, test the email service by:
1. Registering a new user
2. Checking if the verification email is received
3. Verifying the email content and format

