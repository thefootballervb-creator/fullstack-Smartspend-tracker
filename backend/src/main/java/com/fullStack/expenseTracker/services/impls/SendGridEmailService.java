package com.fullStack.expenseTracker.services.impls;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.services.NotificationService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import jakarta.mail.MessagingException;

@Component
@Primary
@ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid", matchIfMissing = false)
public class SendGridEmailService implements NotificationService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:Company}")
    private String fromName;

    private SendGrid getSendGrid() {
        return new SendGrid(sendGridApiKey);
    }

    @Override
    public void sendUserRegistrationVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = Objects.requireNonNull(user.getEmail(), "User email cannot be null");
        String subject = "Please verify your registration";
        
        String htmlContent = "Dear " + user.getUsername() + ",<br><br>"
                + "<p>Thank you for joining us! We are glad to have you on board.</p><br>"
                + "<p>To complete the sign up process, enter the verification code in your device.</p><br>"
                + "<p>verification code: <strong>" + user.getVerificationCode() + "</strong></p><br>"
                + "<p><strong>Please note that the above verification code will be expired within 15 minutes.</strong></p>"
                + "<br>Thank you,<br>"
                + fromName;

        sendEmail(toAddress, subject, htmlContent);
    }

    @Override
    public void sendForgotPasswordVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = Objects.requireNonNull(user.getEmail(), "User email cannot be null");
        String subject = "Forgot password - Please verify your Account";
        
        String htmlContent = "Dear " + user.getUsername() + ",<br><br>"
                + "<p>To change your password, enter the verification code in your device.</p><br>"
                + "<p>verification code: <strong>" + user.getVerificationCode() + "</strong></p><br>"
                + "<p><strong>Please note that the above verification code will be expired within 15 minutes.</strong></p>"
                + "<br>Thank you,<br>"
                + fromName;

        sendEmail(toAddress, subject, htmlContent);
    }

    private void sendEmail(String toAddress, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toAddress);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = getSendGrid();
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                // Email sent successfully
            } else {
                throw new MessagingException("Failed to send email via SendGrid. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new MessagingException("Error sending email via SendGrid: " + ex.getMessage(), ex);
        }
    }
}

